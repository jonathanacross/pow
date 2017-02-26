package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.Constants;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.ProtoTranslator;
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
    private int level;
    private ProtoTranslator translator;
    private List<String> monsterIds;

    // TODO: clean up naming: no _'s for starting variables, constants in all caps.
    static final Point[] _offsets = {
            new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(-1, 1), new Point(-1, 0), new Point(-1, -1), new Point(0, -1), new Point(1, -1)};

    // Number of groups of '1's in the 8 neighbours around a central cell.
    // The encoding is binary, lsb is to the right, then clockwise.
    static int[] _neighborGrpTable = {
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

    public Delve(int width, int height, int level, ProtoTranslator translator, List<String> monsterIds) {
        // reasonable defaults for this game
        this(width, height, 1, 8, 5, level, translator, monsterIds);
    }

    public Delve(int width, int height, int neighborMin, int neighborMax, int connChance,
                 int level, ProtoTranslator translator, List<String> monsterIds) {
        assert(1 <= neighborMin && neighborMin <= 3);
        assert(neighborMin <= neighborMax && neighborMax <= 8);
        assert(0 <= connChance && connChance <= 100);

        this.width = width;
        this.height = height;
        this.neighborMin = neighborMin;
        this.neighborMax = neighborMax;
        this.connChance = connChance;
        this.level = level;
        this.translator = translator;
        this.monsterIds = monsterIds;
    }

    @Override
    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {
        int[][] data = genMap(width, height, rng);
        data = GeneratorUtils.trimMap(data);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, this.translator);

        // place the exits and get key locations
        String upstairsFeatureId = translator.getFeature(Constants.FEATURE_UP_STAIRS).id;
        String downstairsFeatureId =  translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id;
        String floorTerrainId = translator.getTerrain(Constants.TERRAIN_FLOOR).id;
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                dungeonSquares,
                floorTerrainId,
                upstairsFeatureId,
                downstairsFeatureId,
                rng);

        // add items
        int numItems = GeneratorUtils.getDefaultNumItems(data, rng);
        GeneratorUtils.addItems(level, dungeonSquares, numItems, rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, this.monsterIds, null);
        return map;
    }


    private int[][] genMap(int width, int height, Random rng) {

        // initialize the map to all walls
        int[][] map = GeneratorUtils.solidMap(width, height);

        int desiredNumCellsToDig = _estimateCellsToDig(width * height, this.neighborMin, this.neighborMax);

        int numDugcells = _cavern(map, width / 2, height / 2,
                this.neighborMin, this.neighborMax, this.connChance, desiredNumCellsToDig,
                Constants.TERRAIN_FLOOR, Constants.TERRAIN_WALL, rng);

        return map;
    }

    // generates a permutation of 0 ... length-1
    // using the standard Knuth shuffle
    int[] _randomPermutation(int length, Random rng) {
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
    boolean _interior(int[][] map, int x, int y) {
        int width = Array2D.width(map);
        int height = Array2D.height(map);
        return ((x >= 1) && (x < width - 1) && (y >= 1) && (y < height - 1));
    }

    // Count neighbours of the given cells that contain terrain 'terrain'
    int _countNeighbors(int[][] map, int x, int y, int terrain) {
        int count = 0;
        for (int i = 0; i < _offsets.length; i++) {
            int px = x + _offsets[i].x;
            int py = y + _offsets[i].y;
            if (_interior(map, px, py) && (map[px][py] == terrain)) {
                count++;
            }
        }
        return count;
    }

    // Examine the 8 neigbours of the given cell, and count the number
    // of separate groups of terrain cells. A groups contains cells that are
    // of the same type (terrain) and are adjacent, including diagonals.
    int _countGroups(int[][] map, int x, int y, int terrain) {

        int bitmap = 0; // lowest bit is the cell to the right, then clockwise

        for (int i = 0; i < _offsets.length; i++) {
            bitmap >>= 1;
            int px = x + _offsets[i].x;
            int py = y + _offsets[i].y;
            if (_interior(map, px, py) && (map[px][py] == terrain)) {
                bitmap |= 0x80;
            }
        }

        return _neighborGrpTable[bitmap];
    }

    // Dig out an ava cell to flo and store its ava neighbours in
    // random order.
    int _digCell(int[][] map, CellStore cstore, int x, int y, int flo, int ava, Random rng) {

        if ((!_interior(map, x, y)) || (map[x][y] != ava)) {
            return 0; // did nothing
        }

        map[x][y] = flo;

        int[] pi = _randomPermutation(_offsets.length, rng);

        for (int i = 0; i < _offsets.length; i++) {
            int j = pi[i];
            int px = x + _offsets[j].x;
            int py = y + _offsets[j].y;
            if (_interior(map, px, py) && (map[px][py] == ava)) {
                cstore.store(px, py);
            }
        }

        return 1; // dug 1 cell
    }

    // Continue digging until cellnum or no more cells in store. Digging is
    // allowed if the terrain in the cell is 'ava'ilable, cell has from
    // ngb_min to ngb_max flo neighbours, and digging won't open new
    // connections; the last condition is ignored with percent chance
    // connchance.
    // returns number of cells dug
    int _delveOn(int[][] map, CellStore cstore, int ngb_min, int ngb_max, int connchance, int cellnum, int flo, int ava, Random rng) {

        int count = 0;

        while ((count < cellnum) && !cstore.isEmpty()) {
            Point p = cstore.getRandomCell(rng);
            int x = p.x;
            int y = p.y;
            int ngb_count = _countNeighbors(map, x, y, flo);
            int ngb_groups = _countGroups(map, x, y, flo);

            if (_interior(map, x, y) && (map[x][y] == ava) && (ngb_count >= ngb_min) && (ngb_count <= ngb_max) && ((ngb_groups <= 1) || (rng.nextInt(100) < connchance))) {
                count += _digCell(map, cstore, x, y, flo, ava, rng);
            }
        }

        return count;
    }

    // Generate a random cavern of cellnum cells.
    int _cavern(int[][] map, int xorig, int yorig, int ngb_min, int ngb_max, int connchance, int cellnum, int flo, int ava, Random rng) {
        CellStore cstore = new CellStore();
        int count = 0;

        cstore.store(xorig, yorig);

        while ((count < 2 * ngb_min) && (count < cellnum) && !cstore.isEmpty()) {
            Point p = cstore.getRandomCell(rng);
            int x = p.x;
            int y = p.y;
            int ngb_count = _countNeighbors(map, x, y, flo);
            int ngb_groups = _countGroups(map, x, y, flo);

            // stay close to origin, ignore ngb_min
            if (_interior(map, x, y) && (map[x][y] == ava) &&
                    (Math.abs(x - xorig) < 2) && (Math.abs(y - yorig) < 2) &&
                    (ngb_count <= ngb_max) &&
                    ((ngb_groups <= 1) || (rng.nextInt(100) < connchance))) {
                count += _digCell(map, cstore, x, y, flo, ava, rng);
            }
        }

        if (count < cellnum) {
            count += _delveOn(map, cstore, ngb_min, ngb_max, connchance, cellnum - count, flo, ava, rng);
        }

        return count;
    }

    // Estimate a sensible number of cells for given ngb_min, ngb_max.
    int _estimateCellsToDig(int totalcells, int ngb_min, int ngb_max) {
        // (first two entries are not used)
        int[] denom = {8, 8, 8, 7, 6, 5, 5, 4, 4, 4, 3, 3};
        return totalcells / denom[ngb_min + ngb_max];
    }

 private static class CellStore {

    List<Point> cells;

    CellStore() {
        cells = new ArrayList<>();
    }

    void store(int x, int y) {
        cells.add(new Point(x, y));
    }

    boolean isEmpty() {return  cells.isEmpty(); }

    // Remove a cell randomly from the store
    Point getRandomCell(Random rng) {
        int index;
        if (cells.size() < 125) {
            index = rng.nextInt(cells.size());
        } else {
            // makes the pattern more "fluffy"
            index = cells.size() - rng.nextInt(25 * (int) Math.round(Math.pow(cells.size(), 0.33))) - 1;
        }

        Point cell = cells.remove(index);
        return cell;
    }
}

}


