package pow.backend.dungeon.gen;

import java.util.Random;

public class Arena implements DungeonGenerator {

    public int[][] genMap(int width, int height, Random rng) {

        int[][] map = new int[width][height];

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                double x = c / (width - 1.0);
                double y = r / (height - 1.0);
                double d = Math.min(Math.min(x, y), Math.min(1.0 - x, 1.0 - y));
                double z = d - 0.5;
                double probWall = 16.0 * z * z * z * z;
                map[c][r] = (rng.nextDouble() < probWall) ?
                        SquareTypes.WALL.value() : SquareTypes.FLOOR.value();
            }
        }

        return map;
    }
}
