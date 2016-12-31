package pow.backend.dungeon.gen.proto;

import pow.backend.dungeon.gen.DungeonGenerator;
import pow.backend.dungeon.gen.SquareTypes;
import pow.util.Array2D;
import pow.util.Point;

import java.util.Random;

// generates a dungeon using rectangle diffusion limited aggregation
// described at http://www.roguebasin.com/index.php?title=Diffusion-limited_aggregation
// Note that this may not work well for very non-square dungeons.
public class ShapeDLA implements DungeonGenerator {

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


    int minRoomSize;// = 3;
    int maxRoomSize;// = 15;

    public ShapeDLA(int minRoomSize, int maxRoomSize) {
        this.minRoomSize = minRoomSize;
        this.maxRoomSize = maxRoomSize;
    }

    private Shape genOutline(int dungeonWidth, int dungeonHeight, Random rng) {
        int minSize = 3;
        int maxSize = 15;

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
            if (map[x][shape.ymin] == SquareTypes.FLOOR.value()) {
                return true;
            }
            if (map[x][shape.ymax - 1] == SquareTypes.FLOOR.value()) {
                return true;
            }
        }
        for (int y = shape.ymin; y < shape.ymax; y++) {
            if (map[shape.xmin][y] == SquareTypes.FLOOR.value()) {
                return true;
            }
            if (map[shape.xmax - 1][y] == SquareTypes.FLOOR.value()) {
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
            if (map[x][y] == SquareTypes.WALL.value()) {
                int wallCount = 0;
                int floorCount = 0;
                for (int i = 0; i < adjs.length; i++) {
                    int m = map[x + adjs[i][0]][y + adjs[i][1]];
                    if (m == SquareTypes.WALL.value()) {
                        wallCount++;
                    } else if (m == SquareTypes.FLOOR.value()) {
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
        int[][] map = GenUtils.solidMap(width, height);

        // initially, put a small 3x3 seed room in the middle
        int midx = width / 2;
        int midy = height / 2;
        for (int x = -1; x < 1; x++) {
            for (int y = -1; y < 1; y++) {
                map[midx + x][midy + y] = SquareTypes.FLOOR.value();
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
                    map[x][shape.ymin] = SquareTypes.FLOOR.value();
                    map[x][shape.ymax - 1] = SquareTypes.FLOOR.value();
                }
                for (int y = shape.ymin; y < shape.ymax; y++) {
                    map[shape.xmin][y] = SquareTypes.FLOOR.value();
                    map[shape.xmax - 1][y] = SquareTypes.FLOOR.value();
                }
            } else {
                // make a solid room
                for (int x = shape.xmin; x < shape.xmax; x++) {
                    for (int y = shape.ymin; y < shape.ymax; y++) {
                        map[x][y] = SquareTypes.FLOOR.value();
                    }
                }
            }
        }

        // finally, add candles to the sides
        int mindim = width < height ? width : height;
        int numCandles = mindim / 2;
        for (int i = 0; i < numCandles; i++) {
            Point pos = findCandlePosition(map, rng);
            map[pos.x][pos.y] = SquareTypes.CANDLEWALL.value();
        }

        return map;
    }
}