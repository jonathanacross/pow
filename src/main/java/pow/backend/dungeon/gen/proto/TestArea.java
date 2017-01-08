package pow.backend.dungeon.gen.proto;

import com.sun.tools.internal.jxc.ap.Const;

import java.util.Random;

public class TestArea implements ProtoGenerator {

    // NOTE: this ignores the input width and height!
    @Override
    public int[][] genMap(int width, int height, Random randSeed) {

        String[] map = {
                "####################",
                "#..................#",
                "#..................#",
                "#..c%%%%%%%%%%%%%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%%%%%%%%%%%%%%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%%%%%%%%%%%%%%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%%%%%%%%%%%%%c..#",
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
                    case '.': data[x][y] = Constants.TERRAIN_FLOOR; break;
                    case '%': data[x][y] = Constants.TERRAIN_DIGGABLE_WALL; break;
                    default: data[x][y] = Constants.TERRAIN_DEBUG; break;
                }
            }
        }

        return data;
    }
}
