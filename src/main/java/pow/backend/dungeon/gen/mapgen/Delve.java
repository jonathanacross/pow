package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.*;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.backend.utils.GeneratorUtils;
import pow.util.Array2D;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// from http://www.roguebasin.com/index.php?title=Delving_a_connected_cavern
// C code at http://angband.pl/files/delve_b.c
public class Delve implements MapGenerator {

    private int width;
    private int height;
    private int neighborMin;  // 1 <= neighborMin <= 3
    private int neighborMax;  // neighborMin <= neighborMax <= 8
    private int connChance;   // 0 <= connChance <= 100
    private ProtoTranslator translator;
    private int level;
    private MonsterIdGroup monsterIds;
    private final GameMap.Flags flags;

    private static final Point[] OFFSETS = {
            new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(-1, 1),
            new Point(-1, 0), new Point(-1, -1), new Point(0, -1), new Point(1, -1)};

    // Number of groups of '1's in the 8 neighbours around a central cell.
    // The encoding is binary, lsb is to the right, then clockwise.
    private static final int[] NEIGHBOR_GROUP_TABLE = {
         // 0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
            0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1,   // 00
            1, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1,   // 10
            1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 2, 2, 2, 2,   // 20
            1, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1,   // 30
            1, 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1,   // 40
            1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1,   // 50
            1, 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1,   // 60
            1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1,   // 70
            1, 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1,   // 80
            2, 2, 3, 2, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1,   // 90
            2, 2, 3, 2, 3, 2, 3, 2, 3, 3, 4, 3, 3, 2, 3, 2,   // a0
            2, 2, 3, 2, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1,   // b0
            1, 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1,   // c0
            1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1,   // d0
            1, 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1,   // e0
            1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1};  // f0

    public Delve(int width, int height, ProtoTranslator translator, MonsterIdGroup monsterIds,
                 int level, GameMap.Flags flags) {
        // reasonable defaults for this game
        this(width, height, 1, 8, 5, translator, monsterIds, level, flags);
    }

    private Delve(int width, int height, int neighborMin, int neighborMax, int connChance,
                 ProtoTranslator translator, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        assert (1 <= neighborMin && neighborMin <= 3);
        assert (neighborMin <= neighborMax && neighborMax <= 8);
        assert (0 <= connChance && connChance <= 100);

        this.width = width;
        this.height = height;
        this.neighborMin = neighborMin;
        this.neighborMax = neighborMax;
        this.connChance = connChance;
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.level = level;
        this.flags = flags;
    }

    @Override
    public GameMap genMap(String id,
                          String name,
                          List<MapConnection> connections,
                          MapPoint.PortalStatus portalStatus,
                          Random rng) {
        int[][] data = genMap(width, height, rng);
        data = GeneratorUtils.trimMap(data);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, this.translator);

        // place the exits and get key locations
        GeneratorUtils.CommonIds commonIds = new GeneratorUtils.CommonIds(
                translator.getTerrain(Constants.TERRAIN_FLOOR).id,
                translator.getFeature(Constants.FEATURE_UP_STAIRS).id,
                translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id,
                translator.getFeature(Constants.FEATURE_OPEN_PORTAL).id,
                translator.getFeature(Constants.FEATURE_CLOSED_PORTAL).id);
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                portalStatus,
                dungeonSquares,
                commonIds,
                rng);

        // add items
        int numItems = GeneratorUtils.getDefaultNumItems(data, rng);
        GeneratorUtils.addItems(level, dungeonSquares, numItems, rng);

        return new GameMap(id, name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), flags,null);
    }


    private int[][] genMap(int width, int height, Random rng) {

        // initialize the map to all walls
        int[][] map = GeneratorUtils.solidMap(width, height);

        int desiredNumCellsToDig = estimateCellsToDig(width * height, this.neighborMin, this.neighborMax);

        cavern(map, width / 2, height / 2,
                this.neighborMin, this.neighborMax, this.connChance, desiredNumCellsToDig,
                Constants.TERRAIN_FLOOR, Constants.TERRAIN_WALL, rng);

        return map;
    }

    // generates a permutation of 0 ... length-1
    // using the standard Knuth shuffle
    private int[] randomPermutation(int length, Random rng) {
        int[] permutation = new int[length];
        for (int i = 0; i < length; i++) {
            permutation[i] = i;
        }

        for (int i = 0; i < length; i++) {
            int j = rng.nextInt(i + 1);
            permutation[i] = permutation[j];
            permutation[j] = i;
        }
        return permutation;
    }

    // Is the location within the borders - with 1 cell margin
    private boolean interior(int[][] map, int x, int y) {
        int width = Array2D.width(map);
        int height = Array2D.height(map);
        return ((x >= 1) && (x < width - 1) && (y >= 1) && (y < height - 1));
    }

    // Count neighbours of the given cells that contain terrain 'terrain'
    private int countNeighbors(int[][] map, int x, int y, int terrain) {
        int count = 0;
        for (Point offset : OFFSETS) {
            int px = x + offset.x;
            int py = y + offset.y;
            if (interior(map, px, py) && (map[px][py] == terrain)) {
                count++;
            }
        }
        return count;
    }

    // Examine the 8 neighbors of the given cell, and count the number
    // of separate groups of terrain cells. A groups contains cells that are
    // of the same type (terrain) and are adjacent, including diagonals.
    private int countGroups(int[][] map, int x, int y, int terrain) {

        int bitmap = 0; // lowest bit is the cell to the right, then clockwise

        for (Point offset : OFFSETS) {
            bitmap >>= 1;
            int px = x + offset.x;
            int py = y + offset.y;
            if (interior(map, px, py) && (map[px][py] == terrain)) {
                bitmap |= 0x80;
            }
        }

        return NEIGHBOR_GROUP_TABLE[bitmap];
    }

    // Dig out an ava cell to flo and store its ava neighbours in
    // random order.
    private int digCell(int[][] map, CellStore cellStore, int x, int y, int flo, int ava, Random rng) {

        if ((!interior(map, x, y)) || (map[x][y] != ava)) {
            return 0; // did nothing
        }

        map[x][y] = flo;

        int[] pi = randomPermutation(OFFSETS.length, rng);

        for (int i = 0; i < OFFSETS.length; i++) {
            int j = pi[i];
            int px = x + OFFSETS[j].x;
            int py = y + OFFSETS[j].y;
            if (interior(map, px, py) && (map[px][py] == ava)) {
                cellStore.store(px, py);
            }
        }

        return 1; // dug 1 cell
    }

    // Continue digging until cellNum or no more cells in store. Digging is
    // allowed if the terrain in the cell is 'ava'ilable, cell has from
    // nbrMin to nbrMax flo neighbours, and digging won't open new
    // connections; the last condition is ignored with percent chance
    // connChance.
    private void delveOn(int[][] map, CellStore cellStore, int nbrMin, int nbrMax, int connChance, int cellNum, int flo, int ava, Random rng) {

        int count = 0; // number of cells dug

        while ((count < cellNum) && cellStore.isNotEmpty()) {
            Point p = cellStore.getRandomCell(rng);
            int x = p.x;
            int y = p.y;
            int nbrCount = countNeighbors(map, x, y, flo);
            int ngb_groups = countGroups(map, x, y, flo);

            if (interior(map, x, y) && (map[x][y] == ava) && (nbrCount >= nbrMin) && (nbrCount <= nbrMax) && ((ngb_groups <= 1) || (rng.nextInt(100) < connChance))) {
                count += digCell(map, cellStore, x, y, flo, ava, rng);
            }
        }
    }

    // Generate a random cavern of cellNum cells.
    private void cavern(int[][] map, int xOrig, int yOrig, int nbrMin, int nbrMax, int connChance, int cellNum, int flo, int ava, Random rng) {
        CellStore cellStore = new CellStore();
        int count = 0;  // number of cells that were dug

        cellStore.store(xOrig, yOrig);

        while ((count < 2 * nbrMin) && (count < cellNum) && cellStore.isNotEmpty()) {
            Point p = cellStore.getRandomCell(rng);
            int x = p.x;
            int y = p.y;
            int nbrCount = countNeighbors(map, x, y, flo);
            int ngb_groups = countGroups(map, x, y, flo);

            // stay close to origin, ignore nbrMin
            if (interior(map, x, y) && (map[x][y] == ava) &&
                    (Math.abs(x - xOrig) < 2) && (Math.abs(y - yOrig) < 2) &&
                    (nbrCount <= nbrMax) &&
                    ((ngb_groups <= 1) || (rng.nextInt(100) < connChance))) {
                count += digCell(map, cellStore, x, y, flo, ava, rng);
            }
        }

        if (count < cellNum) {
            delveOn(map, cellStore, nbrMin, nbrMax, connChance, cellNum - count, flo, ava, rng);
        }
    }

    // Estimate a sensible number of cells for given nbrMin, nbrMax.
    private int estimateCellsToDig(int totalCells, int nbrMin, int nbrMax) {
        // (first two entries are not used)
        int[] denom = {8, 8, 8, 7, 6, 5, 5, 4, 4, 4, 3, 3};
        return totalCells / denom[nbrMin + nbrMax];
    }

    private static class CellStore {

        final List<Point> cells;

        CellStore() {
            cells = new ArrayList<>();
        }

        void store(int x, int y) {
            cells.add(new Point(x, y));
        }

        boolean isNotEmpty() {
            return !cells.isEmpty();
        }

        // Remove a cell randomly from the store
        Point getRandomCell(Random rng) {
            int index;
            if (cells.size() < 125) {
                index = rng.nextInt(cells.size());
            } else {
                // makes the pattern more "fluffy"
                index = cells.size() - rng.nextInt(25 * (int) Math.round(Math.pow(cells.size(), 0.33))) - 1;
            }

            return cells.remove(index);
        }
    }

}


