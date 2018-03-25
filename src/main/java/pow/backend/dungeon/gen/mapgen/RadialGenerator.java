package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.Constants;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.ProtoTranslator;
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
    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {
        int[][] data = genMap(rng);
        data = GeneratorUtils.trimMap(data);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, this.translator);

        // place the exits and get key locations
        String upstairsFeatureId = translator.getFeature(Constants.FEATURE_UP_STAIRS).id;
        String downstairsFeatureId = translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id;
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

        return new GameMap(name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), flags, null);
    }


    private static class MazeCell {
        Point loc;
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
            cell.available = rng.nextInt(100) < matchPercent
                    ? parent.available : !parent.available;

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
            grid[loc.x - left + 1][loc.y - top + 1] = cell.available ?
                    Constants.TERRAIN_FLOOR :
                    Constants.TERRAIN_LAVA;
        }

//        // mark the start point
//        grid[1 - left][1 - top] = '@';

        return grid;
    }

    private int[][] genMap(Random rng) {
        Map<Point, MazeCell> cells = genCells(rng);
        int[][] grid = toGrid(cells);
        return grid;
    }

//    public static void main(String[] args) {
//        RadialGenerator mp = new RadialGenerator();
//        mp.lavaMaze();
//        char[][] grid = mp.toGrid();
//
//        for (int y = 0; y < grid[0].length; y++) {
//            for (int x = 0; x < grid.length; x++) {
//                System.out.print(grid[x][y]);
//            }
//            System.out.println();
//        }
//    }
}
