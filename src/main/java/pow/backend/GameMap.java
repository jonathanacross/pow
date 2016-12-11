package pow.backend;

import java.io.Serializable;
import java.util.Random;

public class GameMap implements Serializable {
    public char[][] map; // indexed by x,y, or c,r
    public int width;
    public int height;

    private char[][] buildArena(int width, int height, int seed) {
        Random rng = new Random(seed);
        char[][] map = new char[width][height];

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                double x = c / (width - 1.0);
                double y = r / (height - 1.0);
                double d = Math.min(Math.min(x, y), Math.min(1.0 - x, 1.0 - y));
                double z = d - 0.5;
                double probWall = 16.0 * z * z * z * z;
                map[c][r] = (rng.nextDouble() < probWall) ? '#' : '.';
            }
        }

        return map;
    }

    public GameMap() {
        width = 40;
        height = 30;
        map = buildArena(width, height, 123);
        map[(int) (width * 0.25)][(int) (height * 0.3)] = 'W';
        map[(int) (width * 0.75)][(int) (height * 0.6)] = 'L';
    }
}
