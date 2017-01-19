package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.Constants;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.ProtoTranslator;
import pow.util.Array2D;
import pow.util.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


// generates a dungeon using rectangle diffusion limited aggregation
// described at http://www.roguebasin.com/index.php?title=Diffusion-limited_aggregation
// Note that this may not work well for very non-square dungeons.
public class ShapeDLA implements MapGenerator {

    private ProtoTranslator translator;
    private List<String> monsterIds;
    private int width;
    private int height;
    private int minRoomSize;
    private int maxRoomSize;

    public ShapeDLA(ProtoTranslator translator, List<String> monsterIds, int width, int height) {
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.width = width;
        this.height = height;
        this.minRoomSize = 3;
        this.maxRoomSize = 15;
    }

    private static class Shape {
        public int xmin;
        public int xmax;
        public int ymin;
        public int ymax;

        public Shape(int xmin, int xmax, int ymin, int ymax) {
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
        }
    }

    public GameMap genMap(String name,
                          // TODO: add exits: at least up/down!
                          Map<String, String> exits,  // name of this exit -> otherAreaId@otherAreaLocName
                          Random rng) {

        int[][] data = genMap(this.width, this.height, rng);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, this.translator);
        Map<String, Point> keyLocations = addStairs(dungeonSquares, exits, rng);
        int numMonsters = (this.width - 1) * (this.height - 1) / 100;
        List<Actor> monsters = GeneratorUtils.createMonsters(dungeonSquares, numMonsters, this.monsterIds, rng);

        GameMap map = new GameMap(name, dungeonSquares, keyLocations, monsters);
        return map;
    }

    // modifies squares and returns keyLocations..
    // TODO: kind of a bad signature
    private Map<String, Point> addStairs(DungeonSquare[][] squares, Map<String, String> exits, Random rng) {
        Map<String, Point> keyLocations = new HashMap<>();
        for (Map.Entry<String, String> entry : exits.entrySet()) {
            String exitName = entry.getKey();
            String target = entry.getValue();
            // TODO: clean up area moving class: two strings relies too much on random convention
            if (exitName.startsWith("up") || exitName.startsWith("down")) {
                // up or down
                boolean up = exitName.startsWith("up");
                String featureId = up
                        ? translator.getFeature(Constants.FEATURE_UP_STAIRS).id
                        : translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id;
                DungeonFeature stairs = GeneratorUtils.buildStairsFeature(featureId, target, up);
                Point loc = GeneratorUtils.findStairsLocation(squares, rng);
                squares[loc.x][loc.y].feature = stairs;
                keyLocations.put(exitName, loc);
            } else {
                // cardinal direction
                // TODO: implement
//                String target = entry.getValue();
//                DungeonSquare square = buildTeleportTile(style.interiors.get(0).terrain, target);
//                Point loc = findExitCoordinates(terrainMap, borders, exitName, rng);
//                squares[loc.x][loc.y] = square;
//                keyLocations.put(exitName, loc);
            }
        }
        return keyLocations;
    }

    private Shape genOutline(int dungeonWidth, int dungeonHeight, Random rng) {
        int width = rng.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
        int height = rng.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
        int locxmin = 1;
        int locymin = 1;
        int locxmax = dungeonWidth - width;
        int locymax = dungeonHeight - height;
        int xmin = rng.nextInt(locxmax - locxmin) + locxmin;
        int ymin = rng.nextInt(locymax - locymin) + locymin;

        return new Shape(xmin, xmin + width, ymin, ymin + height);
    }

    private boolean touches(int[][] map, Shape shape) {
        for (int x = shape.xmin; x < shape.xmax; x++) {
            if (map[x][shape.ymin] == Constants.TERRAIN_FLOOR) {
                return true;
            }
            if (map[x][shape.ymax - 1] == Constants.TERRAIN_FLOOR) {
                return true;
            }
        }
        for (int y = shape.ymin; y < shape.ymax; y++) {
            if (map[shape.xmin][y] == Constants.TERRAIN_FLOOR) {
                return true;
            }
            if (map[shape.xmax - 1][y] == Constants.TERRAIN_FLOOR) {
                return true;
            }
        }
        return false;
    }

    private Shape perturb(int[][] map, Shape shape, Random rng) {
        int dx = rng.nextInt(3) - 1;
        int dy = rng.nextInt(3) - 1;
        int width = Array2D.width(map);
        int height = Array2D.height(map);
        int midx = width / 2;
        int midy = height / 2;

        // if the shape is at the edge, force the direction to move
        if (shape.xmin == 1) {
            dx = 1;
        }
        if (shape.ymin == 1) {
            dy = 1;
        }
        if (shape.xmax == width) {
            dx = -1;
        }
        if (shape.ymax == height) {
            dy = -1;
        }

        // if dx or dy = 0, then move towards center (for fast convergence)
        if (dx == 0) {
            dx = shape.xmin < midx ? 1 : -1;
        }
        if (dy == 0) {
            dy = shape.ymin < midy ? 1 : -1;
        }

        return new Shape(shape.xmin + dx, shape.xmax + dx, shape.ymin + dy, shape.ymax + dy);
    }

    private Point findCandlePosition(int[][] map, Random rng) {
        int[][] adjs = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}};

        int width = Array2D.width(map);
        int height = Array2D.height(map);

        for ( ; ; ) {
            int x = rng.nextInt(width - 2) + 1;
            int y = rng.nextInt(height - 2) + 1;
            if (map[x][y] == Constants.TERRAIN_WALL) {
                int wallCount = 0;
                int floorCount = 0;
                for (int[] adj : adjs) {
                    int m = map[x + adj[0]][y + adj[1]];
                    if (m == Constants.TERRAIN_WALL) {
                        wallCount++;
                    } else if (m == Constants.TERRAIN_FLOOR) {
                        floorCount++;
                    }
                }
                if (wallCount == 3 && floorCount == 1) {
                    return new Point(x, y);
                }
            }
        }
    }

    public int[][] genMap(int width, int height, Random rng) {

        // initialize the map to all walls
        int[][] map = GeneratorUtils.solidMap(width, height);

        // initially, put a small 3x3 seed room in the middle
        int midx = width / 2;
        int midy = height / 2;
        for (int x = -1; x < 1; x++) {
            for (int y = -1; y < 1; y++) {
                map[midx + x][midy + y] = Constants.TERRAIN_FLOOR;
            }
        }

        // now, do DLA
        for (int iters = 0; iters < 40; iters++) {
            Shape shape = genOutline(width, height, rng);
            while (!touches(map, shape)) {
                shape = perturb(map, shape, rng);
            }

            // hit target, aggregate
            if (iters % 4 != 0) {
                // make just an outline
                for (int x = shape.xmin; x < shape.xmax; x++) {
                    map[x][shape.ymin] = Constants.TERRAIN_FLOOR;
                    map[x][shape.ymax - 1] = Constants.TERRAIN_FLOOR;
                }
                for (int y = shape.ymin; y < shape.ymax; y++) {
                    map[shape.xmin][y] = Constants.TERRAIN_FLOOR;
                    map[shape.xmax - 1][y] = Constants.TERRAIN_FLOOR;
                }
            } else {
                // make a solid room
                for (int x = shape.xmin; x < shape.xmax; x++) {
                    for (int y = shape.ymin; y < shape.ymax; y++) {
                        map[x][y] = Constants.TERRAIN_FLOOR;
                    }
                }
            }
        }

        // finally, add candles to the sides
        int mindim = width < height ? width : height;
        int numCandles = mindim / 2;
        for (int i = 0; i < numCandles; i++) {
            Point pos = findCandlePosition(map, rng);
            map[pos.x][pos.y] |= Constants.FEATURE_CANDLE;
        }

        return map;
    }
}