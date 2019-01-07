package pow.backend.dungeon.gen.worldgen;

import pow.backend.GameConstants;
import pow.backend.GameMap;
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

    public static List<MapPoint> getMapPoints(boolean testWorld) {
        return testWorld ? testInstance.mapPoints : instance.mapPoints;
    }

    private static final String OPEN_PORTAL_ID = "blue portal";
    private static final String CLOSED_PORTAL_ID = "gray portal";

    private static final DungeonTerrain WATER = TerrainData.getTerrain("water 1");
    private static final DungeonTerrain LAVA  = TerrainData.getTerrain("lava");
    private static final DungeonTerrain DEBUG  = TerrainData.getTerrain("debug");
    private static final DungeonTerrain NOTHING  = TerrainData.getTerrain("nothing");

    private static final DungeonFeature NONE = null;
    private static final DungeonFeature CANDLE = FeatureData.getFeature("candle");

    private static final DungeonFeature CANDELABRA = FeatureData.getFeature("candelabra");
    private static final DungeonFeature WOOD_CHEST = FeatureData.getFeature("wood chest");
    private static final DungeonFeature CRATE = FeatureData.getFeature("crate");
    private static final DungeonFeature GLASS_ORB = FeatureData.getFeature("glass orb");
    private static final DungeonFeature BOOKCASE = FeatureData.getFeature("bookcase");
    private static final DungeonFeature RED_CURTAIN = FeatureData.getFeature("red curtain");
    private static final DungeonFeature BLUE_CURTAIN = FeatureData.getFeature("blue curtain");
    private static final DungeonFeature GREEN_CURTAIN = FeatureData.getFeature("green curtain");
    private static final DungeonFeature RED_CARPET = FeatureData.getFeature("red carpet");
    private static final DungeonFeature THRONE = FeatureData.getFeature("throne");
    private static final DungeonFeature PEARL_TILE = FeatureData.getFeature("pearl tile");
    private static final DungeonFeature FIRE_URN = FeatureData.getFeature("fire urn");

    private static final DungeonFeature FOUNTAIN = FeatureData.getFeature("fountain");
    private static final DungeonFeature INN_DOOR = FeatureData.getFeature("inn");
    private static final DungeonFeature WEAPON_SHOP_DOOR = FeatureData.getFeature("weapon shop");
    private static final DungeonFeature MAGIC_SHOP_DOOR = FeatureData.getFeature("magic shop");
    private static final DungeonFeature JEWELER_SHOP_DOOR = FeatureData.getFeature("jeweler shop");
    private static final DungeonFeature OPEN_PORTAL = FeatureData.getFeature(OPEN_PORTAL_ID);
    private static final DungeonFeature CLOSED_PORTAL = FeatureData.getFeature(CLOSED_PORTAL_ID);
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
        if (line.length != 13) {
            throw new RuntimeException("error: expected 13 fields in line: " + String.join(",", line));
        }

        String id = line[0];
        String name = line[1];

        int level = Integer.parseInt(line[2]);

        String groupStr = line[3];
        int group = groupStr.isEmpty() ? Integer.MIN_VALUE : Integer.parseInt(groupStr);

        List<Direction> directions = new ArrayList<>();
        if (!line[4].isEmpty()) {
            String[] dirs = line[4].split(",", -1);
            for (String d: dirs) {
                directions.add(Direction.valueOf(d));
            }
        }

        Set<Integer> fromGroups = new HashSet<>();
        if (!line[5].isEmpty()) {
            String[] groups = line[5].split(",", -1);
            for (String g: groups) {
                fromGroups.add(Integer.parseInt(g));
            }
        }

        Set<String> fromIds = new HashSet<>();
        if (!line[6].isEmpty()) {
            String[] fids = line[6].split(",", -1);
            Collections.addAll(fromIds, fids);
        }

        MapPoint.PortalStatus portalStatus = parsePortalStatus(line[7]);
        String generatorName = line[8];
        String generatorParams = line[9];
        GameMap.Flags flags = parseFlags(line[10]);
        String bossId = getBossId(line[11]);
        List<String> monsterIds = getMonsterIds(line[12]);
        MonsterIdGroup monsterIdGroup = new MonsterIdGroup(monsterIds, bossId != null, bossId);
        MapGenerator generator = buildGenerator(generatorName, generatorParams, monsterIdGroup, level, flags);

        return new MapPoint(id, name, level, group, directions, fromGroups, fromIds, portalStatus, generator);
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
            return new ArrayList<>(MonsterGenerator.getMonsterIds());
        }
        if (field.isEmpty()) {
            return new ArrayList<>();
        }
        String[] monsterIds = field.split(",", -1);
        // validate! important otherwise we might not detect a crashing error until late in the game
        Set<String> allMonsterIds = MonsterGenerator.getMonsterIds();
        for (String monsterId: monsterIds) {
            if (!allMonsterIds.contains(monsterId)) {
                throw new RuntimeException("error: when reading levels: unknown monster id '" + monsterId + "'");
            }
        }

        return new ArrayList<>(Arrays.asList(monsterIds));
    }


    private static GameMap.Flags parseFlags(String text) {
        String[] tokens = text.split(",", -1);

        boolean permLight = false;
        boolean outside = false;
        boolean poisonGas = false;
        boolean hot = false;
        for (String t : tokens) {
            switch (t) {
                case "": break;  // will happen if we have an empty string
                case "permLight": permLight = true; break;
                case "outside": outside = true; break;
                case "poisonGas": poisonGas = true; break;
                case "hot": hot = true; break;
                default:
                    throw new IllegalArgumentException("unknown map flag '" + t + "'");
            }
        }

        return new GameMap.Flags(permLight, outside, poisonGas, hot);
    }

    private static MapPoint.PortalStatus parsePortalStatus(String text) {
        switch (text) {
            case "":
            case "none":
                return MapPoint.PortalStatus.NONE;
            case "open": return MapPoint.PortalStatus.OPEN;
            case "closed": return MapPoint.PortalStatus.CLOSED;
            default:
                throw new IllegalArgumentException("unknown teleport status '" + text + "'");
        }
    }

    private static MapGenerator buildGenerator(String generatorType, String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        switch (generatorType) {
            case "town": return buildTownGenerator(params, monsterIds, level, flags);
            case "recursiveInterpolation": return buildRecursiveInterpolationGenerator(params, monsterIds, level, flags);
            case "shapeDLA": return buildShapeDLAGenerator(params, monsterIds, level, flags);
            case "delve": return buildDelveGenerator(params, monsterIds, level, flags);
            case "cellularAutomata": return buildCellularAutomataGenerator(params, monsterIds, level, flags);
            case "premade": return buildPremadeGenerator(params, monsterIds, level, flags);
            case "mountain": return buildMountainGenerator(params, monsterIds, level, flags);
            case "rogue": return buildRogueGenerator(params, monsterIds, level, flags);
            case "radial": return buildRadialGenerator(params, monsterIds, level, flags);
            case "ripple": return buildRippleGenerator(params, monsterIds, level, flags);
            case "terrain test":
            case "run test":
            case "item test":
            case "arena":
                return buildTestGenerator(generatorType, params, monsterIds, level, flags);
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
        terrainMap.put(Constants.TERRAIN_NOTHING, NOTHING);

        Map<Integer, DungeonFeature> featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_NONE, NONE);
        featureMap.put(Constants.FEATURE_CANDLE, CANDLE);
        featureMap.put(Constants.FEATURE_CANDELABRA, CANDELABRA);
        featureMap.put(Constants.FEATURE_WOOD_CHEST, WOOD_CHEST);
        featureMap.put(Constants.FEATURE_CRATE, CRATE);
        featureMap.put(Constants.FEATURE_GLASS_ORB, GLASS_ORB);
        featureMap.put(Constants.FEATURE_BOOKCASE, BOOKCASE);
        featureMap.put(Constants.FEATURE_RED_CURTAIN, RED_CURTAIN);
        featureMap.put(Constants.FEATURE_BLUE_CURTAIN, BLUE_CURTAIN);
        featureMap.put(Constants.FEATURE_GREEN_CURTAIN, GREEN_CURTAIN);
        featureMap.put(Constants.FEATURE_RED_CARPET, RED_CARPET);
        featureMap.put(Constants.FEATURE_THRONE, THRONE);
        featureMap.put(Constants.FEATURE_FOUNTAIN, FOUNTAIN);
        featureMap.put(Constants.FEATURE_PEARL_TILE, PEARL_TILE);
        featureMap.put(Constants.FEATURE_FIRE_URN, FIRE_URN);
        featureMap.put(Constants.FEATURE_WEAPON_SHOP_DOOR, WEAPON_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_MAGIC_SHOP_DOOR, MAGIC_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_INN_DOOR, INN_DOOR);
        featureMap.put(Constants.FEATURE_JEWELER_SHOP_DOOR, JEWELER_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_OPEN_PORTAL, OPEN_PORTAL);
        featureMap.put(Constants.FEATURE_CLOSED_PORTAL, CLOSED_PORTAL);

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
            case "black tower":  // black tower, off of area 3
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("wood floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("dark brick wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable dark brick wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("dark brick stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("dark brick stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("dark brick open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("dark brick closed door"));
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
            case "gruecave":  // grue's cave, similar to crypt but lava is replaced with a wall
                terrainMap.put(Constants.TERRAIN_LAVA, TerrainData.getTerrain("ivy stone wall"));
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("charcoal floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("ivy stone wall"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable ivy stone wall"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("ivy stone stairs up"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("ivy stone stairs down"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("ivy stone open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("ivy stone closed door"));
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
            case "volcano": // outside level 8
                terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("cold lava floor"));
                terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("rock"));
                terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable rock"));

                featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("tower"));
                featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("dungeon entrance"));
                featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("open door"));
                featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("closed door"));
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

    private static MapGenerator buildTownGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        ProtoTranslator style = getProtoTranslator(params);
        return new Town(style, monsterIds, level, flags);
    }

    private static MapGenerator buildShapeDLAGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        ProtoTranslator style = getProtoTranslator(params);
        return new ShapeDLA(GameConstants.DEFAULT_AREA_SIZE, GameConstants.DEFAULT_AREA_SIZE, style, monsterIds, level, flags);
    }

    private static MapGenerator buildRogueGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        String[] subParams = params.split(",", 2);
        int vaultLevel = Integer.parseInt(subParams[0]);
        ProtoTranslator style = getProtoTranslator(subParams[1]);
        int areaSize = GameConstants.DEFAULT_AREA_SIZE;
        switch (vaultLevel) {
            case 0: areaSize = GameConstants.DEFAULT_AREA_SIZE; break;
            case 1: areaSize = (int) Math.round(GameConstants.DEFAULT_AREA_SIZE * 1.5); break;
            case 2: areaSize = GameConstants.DEFAULT_AREA_SIZE * 2; break;
            default: break;
        }
        return new RogueGenerator(areaSize, areaSize, vaultLevel, style, monsterIds, level, flags);
    }

    private static MapGenerator buildRadialGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        ProtoTranslator style = getProtoTranslator(params);
        return new RadialGenerator(GameConstants.RADIAL_NUM_CELLS, GameConstants.RADIAL_MATCH_PERCENT, style, monsterIds, level, flags);
    }

    private static MapGenerator buildRippleGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        String[] subParams = params.split(",", 2);
        String upstairsId = subParams[0];
        String downstairsId = subParams[1];
        int size = GameConstants.DEFAULT_AREA_SIZE;
        String wallTerrainId = "rock";
        String floorTerrainId = "sand";
        String feature1Id = "spikes";
        String feature2Id = "dead tree";
        String feature3Id = "light pebbles";
        RippleGenerator.MapStyle style = new RippleGenerator.MapStyle(
                wallTerrainId, floorTerrainId, feature1Id, feature2Id, feature3Id,
                upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID );
        return new RippleGenerator(size, style, monsterIds, level, flags);
    }

    private static MapGenerator buildDelveGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        ProtoTranslator style = getProtoTranslator(params);
        return new Delve(GameConstants.DELVE_AREA_SIZE, GameConstants.DELVE_AREA_SIZE, style, monsterIds, level, flags);
    }

    private static MapGenerator buildCellularAutomataGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        String[] subParams = params.split(",", 3);
        int layers = Integer.parseInt(subParams[0]);
        boolean makeLakes = Boolean.parseBoolean(subParams[1]);
        ProtoTranslator style = getProtoTranslator(subParams[2]);
        return new CellularAutomata(GameConstants.DEFAULT_AREA_SIZE, GameConstants.DEFAULT_AREA_SIZE, layers, makeLakes, style, monsterIds, level, flags);
    }

    private static MapGenerator buildTestGenerator(String type, String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        ProtoTranslator style = getProtoTranslator(params);
        return new TestArea(type, style, monsterIds, level, flags);
    }

    private static MapGenerator buildPremadeGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        String[] subParams = params.split(",", 2);
        PremadeMapData.PremadeMapInfo mapInfo = PremadeMapData.getArea(subParams[0]);
        ProtoTranslator style = getProtoTranslator(subParams[1]);
        return new PremadeGenerator(mapInfo, style, monsterIds, level, flags);
    }

    private static MapGenerator buildMountainGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        String[] subParams = params.split(",", 2);
        String upstairsId = subParams[0];
        String downstairsId = subParams[1];
        MountainGenerator.MapStyle mapStyle = new MountainGenerator.MapStyle(
                new double[]{0.08, 0.16, 0.24, 0.32, 0.4, 0.5},
                new TerrainFeatureTriplet[]{
                        new TerrainFeatureTriplet("waves", null, null),
                        new TerrainFeatureTriplet("water 4", null, null),
                        new TerrainFeatureTriplet("water 3", null, null),
                        new TerrainFeatureTriplet("water 2", null, null),
                        new TerrainFeatureTriplet("water 1", null, null),
                        new TerrainFeatureTriplet("sand", null, "palm tree"),
                        new TerrainFeatureTriplet("grass", null, "palm tree")
                },
                upstairsId,
                downstairsId,
                OPEN_PORTAL_ID,
                CLOSED_PORTAL_ID
        );
        return new MountainGenerator(GameConstants.ISLAND_AREA_NUM_ITERATIONS, mapStyle, monsterIds, level, flags);
    }

    private static MapGenerator buildRecursiveInterpolationGenerator(String params, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        String[] subParams = params.split(",", 3);
        String styleName = subParams[0];
        String upstairsId = subParams[1];
        String downstairsId = subParams[2];
        RecursiveInterpolation.MapStyle style;
        switch (styleName) {
            case "grass":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("grass", "bush", "big tree"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        false, null, null);
                break;
            case "desert":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("tan rock", null, null),
                        new TerrainFeatureTriplet("sand", "cactus", "light pebbles"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        false, null, null);
                break;
            case "forest":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("forest", "big tree", "pine tree"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        true,
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("forest","key locked rock door", null));
                break;
            case "water":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("waves", null, null),
                        new TerrainFeatureTriplet("water 3", null, "water 4"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        false, null, null);
                break;
            case "snow":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("snowy rock", null, null),
                        new TerrainFeatureTriplet("snow", "snowy pine tree", "white small tree"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        false, null, null);
                break;
            case "swamp":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("swamp", "poison flower", "sick big tree"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        false, null, null);
                break;
            case "haunted forest":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("tan rock", null, null),
                        new TerrainFeatureTriplet("tall grass", "berry bush", "tombstone"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        false, null, null);
                break;
            case "volcano":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("cold lava floor", null, "dark pebbles"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        false, null, null);
                break;
            case "dig desert":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("dark sand", "dead tree", "light pebbles"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        true,
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("diggable rock", null, null));
                break;
            case "gold desert":
                style = new RecursiveInterpolation.MapStyle(
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("sand", "gold tree", "light pebbles"),
                        upstairsId, downstairsId, OPEN_PORTAL_ID, CLOSED_PORTAL_ID,
                        true,
                        new TerrainFeatureTriplet("rock", null, null),
                        new TerrainFeatureTriplet("sand", "pearl locked rock door", null));
                break;
            default:
                throw new RuntimeException("Unknown MapStyle '" + params + "'");
        }

        return new RecursiveInterpolation(GameConstants.OUTSIDE_AREA_SOURCE_SIZE,
                GameConstants.OUTSIDE_AREA_NUM_INTERPOLATION_STEPS, style, monsterIds, level, flags);
    }
}
