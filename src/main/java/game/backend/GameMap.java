package game.backend;

/**
 * Created by jonathan on 11/4/16.
 */
public class GameMap {
    public char[][] map;
    public int width;
    public int height;

    public GameMap() {
        width = 30;
        height = 30;
        map = new char[width][height];
        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                if (r == 0 || c == 0 || r == height-1 || c == width-1) {
                    map[r][c] = '#';
                } else {
                    map[r][c] = '.';
                }
            }
        }
        map[7][3] = 'W';
        map[7][26] = 'L';
    }
}
