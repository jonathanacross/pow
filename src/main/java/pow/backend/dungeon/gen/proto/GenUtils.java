package pow.backend.dungeon.gen.proto;

import pow.util.Array2D;
import pow.util.Point;

import java.util.Stack;


public class GenUtils {

    // generates a solid wall of dungeon of the desired size
    public static int[][] solidMap(int width, int height) {

        int[][] data = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data[x][y] = Constants.TERRAIN_WALL;
            }
        }

        return data;
    }

    // adapted from http://lodev.org/cgtutor/floodfill.html
    public static int[][] floodFill(int[][] data, int lx, int ly, int newColor, int oldColor) {

        if (oldColor == newColor) {
            return data;
        }

        int w = Array2D.width(data);
        int h = Array2D.height(data);

        Stack<Point> stack = new Stack<>();
        stack.add(new Point(lx, ly));

        while (stack.size() > 0) {
            Point loc = stack.pop();
            int x = loc.x;
            int y = loc.y;
            int y1 = y;
            while (y1 >= 0 && data[x][y1] == oldColor) {
                y1--;
            }
            y1++;
            boolean spanLeft = false;
            boolean spanRight = false;
            while (y1 < h && data[x][y1] == oldColor) {
                data[x][y1] = newColor;
                if (!spanLeft && x > 0 && data[x - 1][y1] == oldColor) {
                    stack.add(new Point(x - 1, y1));
                    spanLeft = true;
                } else if (spanLeft && x > 0 && data[x - 1][y1] != oldColor) {
                    spanLeft = false;
                }
                if (!spanRight && x < w - 1 && data[x + 1][y1] == oldColor) {
                    stack.add(new Point(x + 1, y1));
                    spanRight = true;
                } else if (spanRight && x < w - 1 && data[x + 1][y1] != oldColor) {
                    spanRight = false;
                }
                y1++;
            }
        }

        return data;
    }

    public static Point findOpenSpace(int[][] data) {
        int h = Array2D.height(data);
        int w = Array2D.width(data);

        // Search starting at the middle, then go back
        // to the top -- if we start at the top, then often
        // the first several rows will not be open, and
        // this will waste time going through such squares.
        int mid = h / 2;

        for (int y = mid; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (data[x][y] == Constants.TERRAIN_FLOOR) return new Point(x, y);
            }
        }
        for (int y = 0; y < mid; y++) {
            for (int x = 0; x < w; x++) {
                if (data[x][y] == Constants.TERRAIN_FLOOR) return new Point(x, y);
            }
        }

        // no point found; return somewhere off the map
        return new Point(-1, -1);
    }

    // useful to draw debug maps
    public static String getMapString(int[][] map) {
        int height = Array2D.height(map);
        int width = Array2D.width(map);

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(getChar(map[x][y]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static char getChar(int x) {
        int feature = Constants.getFeature(x);
        // if there's a feature, draw it
        if (feature != Constants.FEATURE_NONE) {
            switch (feature) {
                case Constants.FEATURE_CLOSED_DOOR: return '+';
                case Constants.FEATURE_OPEN_DOOR: return '\'';
                case Constants.FEATURE_CANDLE: return 'c';
                case Constants.FEATURE_WIN_TILE: return 'W';
                case Constants.FEATURE_LOSE_TILE: return 'L';
                default: throw new IllegalArgumentException("unknown feature " + feature);
            }
        } else {
            // draw the terrain
            int terrain = Constants.getTerrain(x);
            switch (terrain) {
                case Constants.TERRAIN_WALL: return '#';
                case Constants.TERRAIN_FLOOR: return '.';
                case Constants.TERRAIN_DIGGABLE_WALL: return '%';
                case Constants.TERRAIN_LAVA: return '~';
                case Constants.TERRAIN_WATER: return 'w';
                case Constants.TERRAIN_DEBUG: return '?';
                default: throw new IllegalArgumentException("unknown terrain " + terrain);
            }
        }
    }
}
