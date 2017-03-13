package pow.backend.dungeon.gen.worldgen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.*;
import pow.backend.dungeon.gen.mapgen.*;
import pow.util.DebugLogger;
import pow.util.Direction;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class WorldDataGen {

    private static final WorldDataGen instance;
    private static final WorldDataGen testInstance;
    private List<MapPoint> mapPoints;

    public static List<MapPoint> getMapPoints() { return instance.mapPoints; }
    public static List<MapPoint> getTestMapPoints() { return testInstance.mapPoints; }


    private static final DungeonTerrain WATER = TerrainData.getTerrain("water 1");
    private static final DungeonTerrain LAVA  = TerrainData.getTerrain("lava");
    private static final DungeonTerrain DEBUG  = TerrainData.getTerrain("debug");

    private static final DungeonFeature NONE = null;
    private static final DungeonFeature WIN_TILE = new DungeonFeature("wintile", "way to win", "orange pearl",
            new DungeonFeature.Flags( false, false, false, false, false, false, false, true),
            new ActionParams());
    private static final DungeonFeature LOSE_TILE = new DungeonFeature("losetile", "death", "cobra",
            new DungeonFeature.Flags( false, false, false, false, false, false, false, true),
            new ActionParams());
    private static final DungeonFeature CANDLE = FeatureData.getFeature("candle");
    private static final DungeonFeature FOUNTAIN = FeatureData.getFeature("fountain");
    private static final DungeonFeature INN_DOOR = FeatureData.getFeature("inn");
    private static final DungeonFeature WEAPON_SHOP_DOOR = FeatureData.getFeature("weapon shop");
    private static final DungeonFeature MAGIC_SHOP_DOOR = FeatureData.getFeature("magic shop");
    static {
        try {
            instance = new WorldDataGen("/data/levels.tsv");
            testInstance = new WorldDataGen("/data/test-levels.tsv");
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private WorldDataGen(String fileName) throws IOException {
        InputStream tsvStream = this.getClass().getResourceAsStream(fileName);
        TsvReader reader = new TsvReader(tsvStream);

        this.mapPoints = new ArrayList<>();
        for (String[] line : reader.getData()) {
            MapPoint roomLinkData = parseMapLinkData(line);
            mapPoints.add(roomLinkData);
        }
    }

    private static MapPoint parseMapLinkData(String[] line) {
        if (line.length != 10) {
            throw new RuntimeException("error: expected 10 fields in line.");
        }

        String id = line[0];

        int level = Integer.parseInt(line[1]);

        String groupStr = line[2];
        int group = groupStr.isEmpty() ? Integer.MIN_VALUE : Integer.parseInt(groupStr);

        List<Direction> directions = new ArrayList<>();
        if (!line[3].isEmpty()) {
            String[] dirs = line[3].split(",");
            for (String d: dirs) {
                directions.add(Direction.valueOf(d));
            }
        }

        Set<Integer> fromGroups = new HashSet<>();
        if (!line[4].isEmpty()) {
            String[] groups = line[4].split(",");
            for (String g: groups) {
                fromGroups.add(Integer.parseInt(g));
            }
        }

        Set<String> fromIds = new HashSet<>();
        if (!line[5].isEmpty()) {
            String[] fids = line[5].split(",");
            for (String fid: fids) {
                fromIds.add(fid);
            }
        }

        String generatorName = line[6];
        String generatorParams = line[7];
        String bossId = getBossId(line[8]);
        List<String> monsterIds = getMonsterIds(line[9]);
        MonsterIdGroup monsterIdGroup = new MonsterIdGroup(monsterIds, bossId != null, bossId);
        MapGenerator generator = buildGenerator(generatorName, generatorParams, monsterIdGroup, level);

        return new MapPoint(id, level, group, directions, fromGroups, fromIds, generator);
    }

    private static String getBossId(String field) {
        if (field.isEmpty()) {
            return null;
        }
        Set<String> allMonsterIds = MonsterGenerator.getMonsterIds();
        // validate! important otherwise we might not detect a crashing error until late in the game
        if (!allMonsterIds.contains(field)) {
            throw new RuntimeException("error: when reading levels: unknown boss id '" + field + "'");
        }
        return field;
    }

    private static List<String> getMonsterIds(String field) {
        if (field.equals(":all:")) {
            return null;
        }
        if (field.isEmpty()) {
            return new ArrayList<>();
        }
        String[] monsterIds = field.split(",");
        // validate! important otherwise we might not detect a crashing error until late in the game
        Set<String> allMonsterIds = MonsterGenerator.getMonsterIds();
        for (String monsterId: monsterIds) {
            if (!allMonsterIds.contains(monsterId)) {
                throw new RuntimeException("error: when reading levels: unknown monster id '" + monsterId + "'");
            }
        }

        return new ArrayList<>(Arrays.asList(monsterIds));
    }

    private static final String STAIRS_UP = "stairs up";
    private static final String STAIRS_DOWN = "stairs down";
    private static final String DUNGEON_ENTRANCE = "dungeon entrance";


    private static MapGenerator buildGenerator(String generatorType, String params, MonsterIdGroup monsterIds, int level) {
        switch (generatorType) {
            case "town": return buildTownGenerator(params, monsterIds, level);
            case "recursiveInterpolation": return buildRecursiveInterpolationGenerator(params, monsterIds, level);
            case "shapeDLA": return buildShapeDLAGenerator(params, monsterIds, level);
            case "delve": return buildDelveGenerator(params, monsterIds, level);
            case "cellularAutomata": return buildCellularAutomataGenerator(params, monsterIds, level);
            case "premade": return buildPremadeGenerator(params, monsterIds, level);
            case "rogue": return buildRogueGenerator(params, monsterIds, level);
            case "terrain test":
            case "run test":
            case "item test":
            case "arena":
                return buildTestGenerator(generatorType, params, monsterIds, level);
            default: throw new RuntimeException("unknown generator type '" + generatorType + "'");
        }
    }

    private static ProtoTranslator getProtoTranslator(String name) {
        // make common entries
        Map<Integer, DungeonTerrain> terrainMap = new HashMap<>();
        terrainMap.put(Constants.TERRAIN_WATER, WATER);
        terrainMap.put(Constants.TERRAIN_LAVA, LAVA);
        terrainMap.put(Constants.TERRAIN_TEMP, DEBUG);
        terrainMap.put(Constants.TERRAIN_DEBUG, DEBUG);

        Map<Integer, DungeonFeature> featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_NONE, NONE);
        featureMap.put(Constants.FEATURE_WIN_TILE, WIN_TILE);
        featureMap.put(Constants.FEATURE_LOSE_TILE, LOSE_TILE);
        featureMap.put(Constants.FEATURE_CANDLE, CANDLE);
        featureMap.put(Constants.FEATURE_FOUNTAIN, FOUNTAIN);
        featureMap.put(Constants.FEATURE_WEAPON_SHOP_DOOR, WEAPON_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_MAGIC_SHOP_DOOR, MAGIC_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_INN_DOOR, INN_DOOR);

        switch (name) {
            case "town":  // towns
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("small stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable small stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("closed door"));
                break;
            case "basement":  // basement of first town
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("wood floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("brown stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable brown stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("brown stone stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("brown stone stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("brown stone open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("brown stone closed door"));
                break;
            case "dungeon":  // e.g,. dungeons 1, 3, 5
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("big stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable big stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("closed door"));
                break;
            case "desert cave":  // dungeon 2
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("dark sand"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("green moss wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable green moss wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("green moss stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("green moss stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("green moss open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("green moss closed door"));
                break;
            case "ice cave": // dungeon 4
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("ice"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("ice wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable ice wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("ice stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("ice stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("ice open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("ice closed door"));
                break;
            case "crypt":  // dungeon 6
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("charcoal floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("ivy stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable ivy stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("ivy stone stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("ivy stone stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("ivy stone open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("ivy stone closed door"));
                break;
            case "brown cave":  // dungeon 7
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("dark sand"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("brown stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable brown stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("brown stone stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("brown stone stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("brown stone open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("brown stone closed door"));
                break;
            case "lava cave":  // dungeon 8
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("cold lava floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("hot stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable hot stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("hot stone stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("hot stone stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("hot stone open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("hot stone closed door"));
                break;
            case "blue":  // dungeon 9
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("blue floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("blue wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable blue wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("blue stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("blue stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("blue open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("blue closed door"));
                break;
            default:
                throw new RuntimeException("unknown ProtoTranslator '" + name + "'");
        }
        return new ProtoTranslator(terrainMap, featureMap);
    }

    private static MapGenerator buildTownGenerator(String params, MonsterIdGroup monsterIds, int level) {
        ProtoTranslator style = getProtoTranslator(params);
        return new Town(style, monsterIds, level);
    }

    private static MapGenerator buildShapeDLAGenerator(String params, MonsterIdGroup monsterIds, int level) {
        ProtoTranslator style = getProtoTranslator(params);
        return new ShapeDLA(60, 60, style, monsterIds, level);
    }

    private static MapGenerator buildRogueGenerator(String params, MonsterIdGroup monsterIds, int level) {
        String[] subParams = params.split(",");
        int vaultLevel = Integer.parseInt(subParams[0]);
        ProtoTranslator style = getProtoTranslator(subParams[1]);
        return new RogueGenerator(60, 60, vaultLevel, style, monsterIds, level);
    }

    private static MapGenerator buildDelveGenerator(String params, MonsterIdGroup monsterIds, int level) {
        ProtoTranslator style = getProtoTranslator(params);
        return new Delve(50, 50, style, monsterIds, level);
    }

    private static MapGenerator buildCellularAutomataGenerator(String params, MonsterIdGroup monsterIds, int level) {
        String[] subParams = params.split(",");
        int layers = Integer.parseInt(subParams[0]);
        boolean makeLakes = Boolean.parseBoolean(subParams[1]);
        ProtoTranslator style = getProtoTranslator(subParams[2]);
        return new CellularAutomata(60, 60, layers, makeLakes, style, monsterIds, level);
    }

    private static MapGenerator buildTestGenerator(String type, String params, MonsterIdGroup monsterIds, int level) {
        ProtoTranslator style = getProtoTranslator(params);
        return new TestArea(type, style, monsterIds, level);
    }

    private static MapGenerator buildPremadeGenerator(String params, MonsterIdGroup monsterIds, int level) {
        String[] subParams = params.split(",");
        PremadeMapData.PremadeMapInfo mapInfo = PremadeMapData.getLevel(subParams[0]);
        ProtoTranslator style = getProtoTranslator(subParams[1]);
        return new PremadeGenerator(mapInfo, style, monsterIds, level);
    }

    private static MapGenerator buildRecursiveInterpolationGenerator(String params, MonsterIdGroup monsterIds, int level) {
        RecursiveInterpolation.MapStyle style;
        switch (params) {
            case "grass":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("grass", "bush", "big tree")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "desert":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("dark sand", "cactus", "light pebbles")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "forest":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("forest", "big tree", "pine tree")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "water":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("waves", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("water 3", null, "water 4")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "snow":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("snowy rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("snow", "snowy pine tree", "white small tree")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "swamp":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("swamp", "poison flower", "sick big tree")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "haunted forest":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("forest", "berry bush", "pine tree")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "volcano":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("cold lava floor", null, "dark pebbles")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "gold desert":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("dark sand", "gold tree", "light pebbles")),
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            default:
                throw new RuntimeException("Unknown MapStyle '" + params + "'");
        }

        return new RecursiveInterpolation(6, 3, style, monsterIds, level);
    }
}
