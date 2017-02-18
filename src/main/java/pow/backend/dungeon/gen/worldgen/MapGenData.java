package pow.backend.dungeon.gen.worldgen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.dungeon.gen.Constants;
import pow.backend.dungeon.gen.FeatureData;
import pow.backend.dungeon.gen.ProtoTranslator;
import pow.backend.dungeon.gen.TerrainData;
import pow.backend.dungeon.gen.mapgen.MapGenerator;
import pow.backend.dungeon.gen.mapgen.RecursiveInterpolation;
import pow.backend.dungeon.gen.mapgen.ShapeDLA;
import pow.backend.dungeon.gen.mapgen.Town;
import pow.util.Direction;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MapGenData {
    public String id;
    public int level;
    public int group;
    public List<Direction> fromDirs;
    public Set<Integer> fromGroups;
    public Set<String> fromIds;
    public MapGenerator mapGenerator;

    public MapGenData(String id,
                      int level,
                      int group,
                      List<Direction> fromDirs,
                      Set<Integer> fromGroups,
                      Set<String> fromIds,
                      MapGenerator mapGenerator) {
        this.id = id;
        this.level = level;
        this.group = group;
        this.fromDirs = fromDirs;
        this.fromGroups = fromGroups;
        this.fromIds = fromIds;
        this.mapGenerator = mapGenerator;
    }

    public static MapGenData parseMapLinkData(String[] line) {
        // TODO: check line length
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
        List<String> monsterIds = splitField(line[8]);
        MapGenerator generator = buildGenerator(generatorName, generatorParams, level, monsterIds);

        return new MapGenData(id, level, group, directions, fromGroups, fromIds, generator);
    }

    // TODO: add consistency checks to the input file:
    // - first room isn't connected to anything
    // - every other room connects either to an id or a group, but not both
    public static List<MapGenData> readLinkData() throws IOException {
        InputStream tsvStream = GenTopoTest.class.getResourceAsStream("/data/levels.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        List<MapGenData> roomLinkDataList = new ArrayList<>();
        for (String[] line : reader.getData()) {
            MapGenData roomLinkData = parseMapLinkData(line);
            roomLinkDataList.add(roomLinkData);
        }

        return roomLinkDataList;
    }

    private static List<String> splitField(String field) {
        if (field.isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = field.split(",");
        return new ArrayList<>(Arrays.asList(parts));
    }

    private static final String STAIRS_UP = "stairs up";
    private static final String STAIRS_DOWN = "stairs down";
    private static final String DUNGEON_ENTRANCE = "dungeon entrance";


    private static MapGenerator buildGenerator(String generatorType, String params, int difficulty, List<String> monsterIds) {
        switch (generatorType) {
            case "town": return buildTownGenerator(params, difficulty, monsterIds);
            case "recursiveInterpolation": return buildRecursiveInterpolationGenerator(params, difficulty, monsterIds);
            case "shapeDLA": return shapeDLAGenerator(params, difficulty, monsterIds);
            default: return null;
        }
    }

    private static DungeonTerrain WATER = TerrainData.getTerrain("water 1");
    private static DungeonTerrain LAVA  = TerrainData.getTerrain("lava");

    private static DungeonFeature WIN_TILE = new DungeonFeature("wintile", "way to win", "orange pearl",
            new DungeonFeature.Flags( false, false, false, false, false, false, false, true),
            new ActionParams());
    private static DungeonFeature LOSE_TILE = new DungeonFeature("losetile", "death", "cobra",
            new DungeonFeature.Flags( false, false, false, false, false, false, false, true),
            new ActionParams());
    private static DungeonFeature CANDLE = FeatureData.getFeature("candle");
    private static DungeonFeature FOUNTAIN = FeatureData.getFeature("fountain");
    private static DungeonFeature INN_DOOR = FeatureData.getFeature("inn");
    private static DungeonFeature WEAPON_SHOP_DOOR = FeatureData.getFeature("weapon shop");
    private static DungeonFeature MAGIC_SHOP_DOOR = FeatureData.getFeature("magic shop");


    private static ProtoTranslator getProtoTranslator(String name) {
        // make common entries
        Map<Integer, DungeonTerrain> terrainMap = new HashMap<>();
        terrainMap.put(Constants.TERRAIN_WATER, WATER);
        terrainMap.put(Constants.TERRAIN_LAVA, LAVA);

        Map<Integer, DungeonFeature> featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, WIN_TILE);
        featureMap.put(Constants.FEATURE_LOSE_TILE, LOSE_TILE);
        featureMap.put(Constants.FEATURE_CANDLE, CANDLE);
        featureMap.put(Constants.FEATURE_FOUNTAIN, FOUNTAIN);
        featureMap.put(Constants.FEATURE_WEAPON_SHOP_DOOR, WEAPON_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_MAGIC_SHOP_DOOR, MAGIC_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_INN_DOOR, INN_DOOR);

        switch (name) {
            case "dungeon":
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("big stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable big stone wall"));
                terrainMap.put(Constants.TERRAIN_WATER, WATER);
                terrainMap.put(Constants.TERRAIN_LAVA, LAVA);

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("closed door"));
                break;
            case "crypt":
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("charcoal floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("ivy stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable ivy stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("ivy stone stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("ivy stone stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("ivy stone open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("ivy stone closed door"));
                break;
            case "building":
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("wood floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("brown stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable brown stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("brown stone stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("brown stone stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("brown stone open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("brown stone closed door"));
                break;
            case "town":
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("small stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable small stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("closed door"));
                break;
            default:
                throw new RuntimeException("unknown ProtoTranslator '" + name + "'");
        }
        return new ProtoTranslator(terrainMap, featureMap);
    }

    private static MapGenerator buildTownGenerator(String params, int difficulty, List<String> monsterIds) {
        return new Town(difficulty, monsterIds);
    }

    private static MapGenerator shapeDLAGenerator(String params, int difficulty, List<String> monsterIds) {
        ProtoTranslator style = getProtoTranslator(params);
        return new ShapeDLA(style, monsterIds, 50, 50, difficulty);
    }

    private static MapGenerator buildRecursiveInterpolationGenerator(String params, int difficulty, List<String> monsterIds) {
        RecursiveInterpolation.MapStyle style = null;
        // TODO: refactor out monsterIds from MapStyle
        switch (params) {
            case "grass":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("grass", "bush", "big tree")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "desert":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("dark sand", "cactus", "light pebbles")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "forest":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("forest", "big tree", "pine tree")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "water":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("waves", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("water 3", null, "water 4")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "snow":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("snowy rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("snow", "snowy pine tree", "white small tree")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "swamp":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("swamp", "poison flower", "sick big tree")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "haunted forest":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("forest", "berry bush", "pine tree")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "volcano":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("cold lava floor", null, "dark pebbles")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            case "gold desert":
                style = new RecursiveInterpolation.MapStyle(
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                        Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("dark sand", "gold tree", "light pebbles")),
                        monsterIds,
                        STAIRS_UP, DUNGEON_ENTRANCE);
                break;
            default:
                throw new RuntimeException("Unknown MapStyle '" + params + "'");
        }

        return new RecursiveInterpolation(6, 3, style, difficulty);
    }
}
