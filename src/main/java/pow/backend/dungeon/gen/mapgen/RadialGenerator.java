package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.Constants;
import pow.backend.utils.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.ProtoTranslator;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.util.Direction;
import pow.util.Point;

import java.util.*;

public class RadialGenerator implements MapGenerator {

    private final int numCells;
    private final int matchPercent;
    private final ProtoTranslator translator;
    private final int level;
    private final MonsterIdGroup monsterIds;
    private final GameMap.Flags flags;

    public RadialGenerator(int numCells, int matchPercent,
                           ProtoTranslator translator,
                           MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        this.numCells = numCells;
        this.matchPercent = matchPercent;
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
        int[][] data = genMap(rng);
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

        return new GameMap(id, name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), flags, null);
    }


    private static class MazeCell {
        public final Point loc;
        public boolean available;
        public boolean seen;

        public MazeCell(Point loc, boolean available) {
            this.loc = loc;
            this.available = available;
            this.seen = false;
        }
    }


    private static MazeCell findOrCreateCell(Map<Point, MazeCell> cells, Point loc) {
        if (!cells.containsKey(loc)) {
            cells.put(loc, new MazeCell(loc, true));
        }
        return cells.get(loc);
    }


    private Map<Point, MazeCell> genCells(Random rng) {
        Map<Point, MazeCell> cells;

        cells = new HashMap<>();
        MazeCell start = new MazeCell(new Point(0, 0), true);
        cells.put(start.loc, start);

        List<MazeCell> toProcess = new ArrayList<>();
        toProcess.add(start);

        for (int i = 0; i < numCells; i++) {
            if (toProcess.isEmpty()) {
                break;
            }
            // pick random cell
            int idx = rng.nextInt(toProcess.size());
            MazeCell cell = toProcess.remove(idx);

            // skip of been here already
            if (cell.seen) {
                continue;
            }

            // mark so we don't re-process
            cell.seen = true;

            Direction parentDir = Direction.getDir(-cell.loc.x, -cell.loc.y);
            Point parentLoc = cell.loc.add(parentDir);
            MazeCell parent = findOrCreateCell(cells, parentLoc);
            cell.available = rng.nextInt(100) < matchPercent == parent.available;

            for (Direction direction : Direction.CARDINALS) {
                Point newLoc = cell.loc.add(direction);
                toProcess.add(findOrCreateCell(cells, newLoc));
            }
        }

        // add a layer of vacant cells around the border
        for (MazeCell cell : toProcess) {
            for (Direction direction : Direction.CARDINALS) {
                Point newLoc = cell.loc.add(direction);
                findOrCreateCell(cells, newLoc); // create makes vacant by default
            }
        }

        return cells;
    }

    private static int[][] toGrid(Map<Point, MazeCell> cells) {
        // establish bounds
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;

        for (MazeCell cell : cells.values()) {
            Point loc = cell.loc;
            if (loc.x < left) {
                left = loc.x;
            }
            if (loc.x > right) {
                right = loc.x;
            }
            if (loc.y < top) {
                top = loc.y;
            }
            if (loc.y > bottom) {
                bottom = loc.y;
            }
        }

        int width = right - left + 3;
        int height = bottom - top + 3;

        int[][] grid = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = Constants.TERRAIN_WALL;
            }
        }

        for (MazeCell cell : cells.values()) {
            Point loc = cell.loc;
            grid[loc.x - left + 1][loc.y - top + 1] = cell.available
                    ? Constants.TERRAIN_FLOOR
                    : Constants.TERRAIN_LAVA;
        }

        // Note: the start point is at grid[1 - left][1 - top].
        // Could use this later if need something in the center of the level.

        return grid;
    }

    private int[][] genMap(Random rng) {
        Map<Point, MazeCell> cells = genCells(rng);
        int[][] grid = toGrid(cells);
        return grid;
    }
}
