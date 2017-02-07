package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.*;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// generates a test area.  Not designed to be configurable.
public class TestArea implements MapGenerator {
    private int level;

    public TestArea(int level) {
        this.level = level;
    }

    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {
        return genItemMap(name, connections, rng);
        //return genPremadeMap(name, connections, rng);
    }

    private GameMap genPremadeMap(String name,
                          List<MapConnection> connections,
                          Random rng) {

        int[][] data = genMapPremade();
        ProtoTranslator translator = new ProtoTranslator(0);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, translator);

        // place the exits and get key locations
        String upstairsFeatureId = translator.getFeature(Constants.FEATURE_UP_STAIRS).id;
        String downstairsFeatureId =  translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id;
        String floorTerrainId = translator.getTerrain(Constants.TERRAIN_FLOOR).id;
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                dungeonSquares,
                floorTerrainId,
                upstairsFeatureId,
                downstairsFeatureId,
                rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, null);
        return map;
    }

    // Creates a map showing all items for all levels.
    public GameMap genItemMap(String name,
            List<MapConnection> connections,
            Random rng) {
        int width = 100;
        int height = 100;
        int[][] data = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data[x][y] =
                        (x == 0 || y == 0 || x == width - 1 || y == height - 1) ?
                                Constants.TERRAIN_WALL + Constants.FEATURE_CANDLE :
                                Constants.TERRAIN_FLOOR;
            }
        }

        ProtoTranslator translator = new ProtoTranslator(1);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, translator);

        // place the exits and get key locations
        String upstairsFeatureId = translator.getFeature(Constants.FEATURE_UP_STAIRS).id;
        String downstairsFeatureId =  translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id;
        String floorTerrainId = translator.getTerrain(Constants.TERRAIN_FLOOR).id;
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                dungeonSquares,
                floorTerrainId,
                upstairsFeatureId,
                downstairsFeatureId,
                rng);

        // add all items
        for (int level = 0; level < 90; level++) {
            List<String> itemIds = ItemGenerator.getItemIdsForLevel(level);
            for (int id = 0; id < itemIds.size(); id++) {
                dungeonSquares[id+1][level+1].items.add(ItemGenerator.genItem(itemIds.get(id), level, rng));
            }
        }

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, null);
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
                "#..%....+.+.....%..#",
                "#..%#####+######%..#",
                "#..%............%..#",
                "#..%............%..#",
                "#..%............%..#",
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
//                    case '>': data[x][y] = Constants.TERRAIN_FLOOR | Constants.FEATURE_DOWN_STAIRS; break;
//                    case '<': data[x][y] = Constants.TERRAIN_FLOOR | Constants.FEATURE_UP_STAIRS; break;
                    default: data[x][y] = Constants.TERRAIN_DEBUG; break;
                }
            }
        }

        return data;
    }
}
