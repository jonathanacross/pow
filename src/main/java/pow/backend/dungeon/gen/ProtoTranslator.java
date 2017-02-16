package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;

import java.util.HashMap;
import java.util.Map;

public class ProtoTranslator {

    private Map<Integer, DungeonTerrain> terrainMap;
    private Map<Integer, DungeonFeature> featureMap;

    public DungeonTerrain getTerrain(int x) {
        return terrainMap.get(Constants.getTerrain(x));
    }
    public DungeonFeature getFeature(int x) {
        return featureMap.get(Constants.getFeature(x));
    }

    // TODO: initialize all data from files
    public ProtoTranslator(int mode) {
        switch (mode) {
            case 0: initBuilding(); break;
            case 1: initStandard(); break;
            case 2: initCrypt(); break;
            case 3: initTown(); break;
        }
    }

    // hardcoded features for things that don't change
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

    private void initStandard() {
        terrainMap = new HashMap<>();
        terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("floor"));
        terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("big stone wall"));
        terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable big stone wall"));
        terrainMap.put(Constants.TERRAIN_WATER, WATER);
        terrainMap.put(Constants.TERRAIN_LAVA, LAVA);

        featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, WIN_TILE);
        featureMap.put(Constants.FEATURE_LOSE_TILE, LOSE_TILE);
        featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("stairs up"));
        featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("stairs down"));
        featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("open door"));
        featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("closed door"));
        featureMap.put(Constants.FEATURE_CANDLE, CANDLE);
        featureMap.put(Constants.FEATURE_FOUNTAIN, FOUNTAIN);
        featureMap.put(Constants.FEATURE_WEAPON_SHOP_DOOR, WEAPON_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_MAGIC_SHOP_DOOR, MAGIC_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_INN_DOOR, INN_DOOR);
    }

    private void initCrypt() {
        terrainMap = new HashMap<>();
        terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("charcoal floor"));
        terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("ivy stone wall"));
        terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable ivy stone wall"));
        terrainMap.put(Constants.TERRAIN_WATER, WATER);
        terrainMap.put(Constants.TERRAIN_LAVA, LAVA);

        featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, WIN_TILE);
        featureMap.put(Constants.FEATURE_LOSE_TILE, LOSE_TILE);
        featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("ivy stone stairs up"));
        featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("ivy stone stairs down"));
        featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("ivy stone open door"));
        featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("ivy stone closed door"));
        featureMap.put(Constants.FEATURE_CANDLE, CANDLE);
        featureMap.put(Constants.FEATURE_FOUNTAIN, FOUNTAIN);
        featureMap.put(Constants.FEATURE_WEAPON_SHOP_DOOR, WEAPON_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_MAGIC_SHOP_DOOR, MAGIC_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_INN_DOOR, INN_DOOR);
    }

    private void initBuilding() {
        terrainMap = new HashMap<>();
        terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("wood floor"));
        terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("brown stone wall"));
        terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable brown stone wall"));
        terrainMap.put(Constants.TERRAIN_WATER, WATER);
        terrainMap.put(Constants.TERRAIN_LAVA, LAVA);

        featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, WIN_TILE);
        featureMap.put(Constants.FEATURE_LOSE_TILE, LOSE_TILE);
        featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("brown stone stairs up"));
        featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("brown stone stairs down"));
        featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("brown stone open door"));
        featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("brown stone closed door"));
        featureMap.put(Constants.FEATURE_CANDLE, CANDLE);
        featureMap.put(Constants.FEATURE_FOUNTAIN, FOUNTAIN);
        featureMap.put(Constants.FEATURE_WEAPON_SHOP_DOOR, WEAPON_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_MAGIC_SHOP_DOOR, MAGIC_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_INN_DOOR, INN_DOOR);
    }

    private void initTown() {
        terrainMap = new HashMap<>();
        terrainMap.put(Constants.TERRAIN_FLOOR, TerrainData.getTerrain("floor"));
        terrainMap.put(Constants.TERRAIN_WALL, TerrainData.getTerrain("small stone wall"));
        terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL, TerrainData.getTerrain("diggable small stone wall"));
        terrainMap.put(Constants.TERRAIN_WATER, WATER);
        terrainMap.put(Constants.TERRAIN_LAVA, LAVA);

        featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, WIN_TILE);
        featureMap.put(Constants.FEATURE_LOSE_TILE, LOSE_TILE);
        featureMap.put(Constants.FEATURE_UP_STAIRS, FeatureData.getFeature("stairs up"));
        featureMap.put(Constants.FEATURE_DOWN_STAIRS, FeatureData.getFeature("stairs down"));
        featureMap.put(Constants.FEATURE_OPEN_DOOR, FeatureData.getFeature("open door"));
        featureMap.put(Constants.FEATURE_CLOSED_DOOR, FeatureData.getFeature("closed door"));
        featureMap.put(Constants.FEATURE_CANDLE, CANDLE);
        featureMap.put(Constants.FEATURE_FOUNTAIN, FOUNTAIN);
        featureMap.put(Constants.FEATURE_WEAPON_SHOP_DOOR, WEAPON_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_MAGIC_SHOP_DOOR, MAGIC_SHOP_DOOR);
        featureMap.put(Constants.FEATURE_INN_DOOR, INN_DOOR);
    }
}


