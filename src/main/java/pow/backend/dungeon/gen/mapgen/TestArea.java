package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.Constants;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.ProtoTranslator;
import pow.util.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// generates a test area.  Not designed to be configurable.
public class TestArea implements MapGenerator {

    public GameMap genMap(String name,
                          // TODO: this is currently ignored; no exits are made
                          Map<String, String> exits,  // name of this exit -> otherAreaId@otherAreaLocName
                          Random rng) {

        int[][] data = genMapPremade();
        ProtoTranslator translator = new ProtoTranslator(0);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, translator);

        int numMonsters = 10;
        List<Actor> monsters = GeneratorUtils.createMonsters(dungeonSquares, numMonsters, null, rng);

        Map<String, Point> keyLocations = new HashMap<>();

        GameMap map = new GameMap(name, dungeonSquares, keyLocations, monsters);
        return map;
    }

    private int[][] genMapPremade() {
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
