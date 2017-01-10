package pow.backend.dungeon.gen.proto;

import java.util.Random;

public class TestArea implements ProtoGenerator {

    // NOTE: this ignores the input width and height!
    @Override
    public int[][] genMap(int width, int height, Random randSeed) {

        String[] map = {
                "####################",
                "#..................#",
                "#..................#",
                "#..c%%%%%'%%%%%%%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%%%%%%+%%%%%%%..#",
                "#..%.........>..%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%..<.........%..#",
                "#..%%%%%%+%%%%%%%..#",
                "#..%wwww....~~~~%..#",
                "#..%w.w......~.~%..#",
                "#..%%%%%%'%%%%%%c..#",
                "#..................#",
                "#..................#",
                "####################"
        };

        int w = map[0].length();
        int h = map.length;

        // start with an empty room
        int[][] data = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                switch (map[y].charAt(x)) {
                    case '#': data[x][y] = Constants.TERRAIN_WALL; break;
                    case 'c': data[x][y] = Constants.TERRAIN_WALL | Constants.FEATURE_CANDLE; break;
                    case 'w': data[x][y] = Constants.TERRAIN_WATER; break;
                    case '~': data[x][y] = Constants.TERRAIN_LAVA; break;
                    case '.': data[x][y] = Constants.TERRAIN_FLOOR; break;
                    case '%': data[x][y] = Constants.TERRAIN_DIGGABLE_WALL; break;
                    case '+': data[x][y] = Constants.TERRAIN_FLOOR | Constants.FEATURE_CLOSED_DOOR; break;
                    case '\'': data[x][y] = Constants.TERRAIN_FLOOR | Constants.FEATURE_OPEN_DOOR; break;
                    case '>': data[x][y] = Constants.TERRAIN_FLOOR | Constants.FEATURE_DOWN_STAIRS; break;
                    case '<': data[x][y] = Constants.TERRAIN_FLOOR | Constants.FEATURE_UP_STAIRS; break;
                    default: data[x][y] = Constants.TERRAIN_DEBUG; break;
                }
            }
        }

        return data;
    }
}
