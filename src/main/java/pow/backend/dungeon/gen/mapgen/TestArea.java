package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.*;
import pow.util.Point;

import java.util.*;

// generates various types of test areas.
public class TestArea implements MapGenerator {

    private String type;
    private ProtoTranslator translator;
    private int level;
    private MonsterIdGroup monsterIds;

    public TestArea(String type, ProtoTranslator translator, MonsterIdGroup monsterIds, int level) {
        this.type = type;
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.level = level;
    }

    @Override
    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {
        switch (type) {
            case "run test":
                return genPremadeMap(name, RUN_TEST, connections, rng);
            case "terrain test":
                return genPremadeMap(name, TERRAIN_TYPES_TEST, connections, rng);
            case "item test":
                return genItemMap(name, connections, rng);
            case "arena":
                return genArena(name, connections, rng);
            default:
                throw new RuntimeException("unknown test area type '" + type + "'");
        }
    }

    private GameMap genPremadeMap(String name,
                                  String[] charData,
                                  List<MapConnection> connections,
                                  Random rng) {
        PremadeMapData.PremadeMapInfo mapInfo = PremadeMapData.parseMapInfo(Arrays.asList(charData));
        PremadeGenerator generator = new PremadeGenerator(mapInfo, translator, monsterIds, level);
        return generator.genMap(name, connections, rng);
    }

    // Creates a map showing all items for all levels.
    private GameMap genItemMap(String name,
            List<MapConnection> connections,
            Random rng) {
        int width = 50;
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

        // add the artifacts
        List<String> artifactIds = new ArrayList<>(ArtifactData.getArtifactIds());
        for (int idx = 0; idx < artifactIds.size(); idx++) {
            String id = artifactIds.get(idx);
            dungeonSquares[width-2][idx+1].items.add(ArtifactData.getArtifact(id));
        }

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), null);
        return map;
    }

    private static final String[] TERRAIN_TYPES_TEST = {
            "terrain types",
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

    private static final String[] RUN_TEST = {
            "run around in here!",
            "####################################",
            "#..................................#",
            "#..................................#",
            "#...###########.........########...#",
            "#...#...................#......#...#",
            "#...#.#########.........#.####.#...#",
            "#...#.#.................#.#....#...#",
            "#...#.#########....######.######...#",
            "#...#..............................#",
            "#...###########....#############...#",
            "#..................................#",
            "#..................................#",
            "#...######+#####...#.##...#####....#",
            "#...#..........#...##.##.##........#",
            "#...#.########.#....##.###.####....#",
            "#...#.#......#.#.....##.#.##.......#",
            "#...#.########.#......##.##........#",
            "#...#..........#.......###.........#",
            "#...############...................#",
            "#..................................#",
            "#....................##########....#",
            "#...##c#####%+%%######........##...#",
            "#.....................########.#...#",
            "#...#####c#########c##......##.#...#",
            "#.....~~~~~~~~~~~.....#######.##...#",
            "#............................###...#",
            "#.........######################...#",
            "#..................................#",
            "####################################"
    };

    private GameMap genArena(String name,
                          List<MapConnection> connections,
                          Random rng) {

        final int width = 60;
        final int height = 60;
        int[][] data = new int[width][height];

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                data[c][r] = (c == 0 || r == 0 || c == width - 1 || r == height - 1) ?
                        Constants.TERRAIN_WALL :
                        Constants.TERRAIN_FLOOR;
            }
        }

        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, this.translator);

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

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), null);
        return map;
    }
}
