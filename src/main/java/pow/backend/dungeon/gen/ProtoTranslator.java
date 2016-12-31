package pow.backend.dungeon.gen;

import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.dungeon.gen.proto.Constants;

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
        }
    }

    private void initStandard() {
        terrainMap = new HashMap<>();
        DungeonTerrain.Flags open = new DungeonTerrain.Flags(false);
        DungeonTerrain.Flags blocked = new DungeonTerrain.Flags(true);
        terrainMap.put(Constants.TERRAIN_FLOOR,
                new DungeonTerrain("floor", "floor", "floor", open));
        terrainMap.put(Constants.TERRAIN_WALL,
                new DungeonTerrain("big stone wall", "big stone wall", "big stone wall", blocked));
        terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL,
                new DungeonTerrain("diggable big stone wall", "diggable big stone wall", "diggable big stone wall", blocked));
        terrainMap.put(Constants.TERRAIN_WATER,
                new DungeonTerrain("water", "water", "water 1", blocked));
        terrainMap.put(Constants.TERRAIN_LAVA,
                new DungeonTerrain("lava", "lava", "lava", blocked));

        featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, new DungeonFeature("wintile", "way to win", "orange pearl",
                new DungeonFeature.Flags(false), 0));
        featureMap.put(Constants.FEATURE_LOSE_TILE, new DungeonFeature("losetile", "death", "cobra",
                new DungeonFeature.Flags(false), 0));
        featureMap.put(Constants.FEATURE_CANDLE, new DungeonFeature("candle", "candle", "candle",
                new DungeonFeature.Flags(false), 3));
    }

    private void initCrypt() {
        terrainMap = new HashMap<>();
        DungeonTerrain.Flags open = new DungeonTerrain.Flags(false);
        DungeonTerrain.Flags blocked = new DungeonTerrain.Flags(true);
        terrainMap.put(Constants.TERRAIN_FLOOR,
                new DungeonTerrain("charcoal floor", "charcoal floor", "charcoal floor", open));
        terrainMap.put(Constants.TERRAIN_WALL,
                new DungeonTerrain("ivy stone wall", "ivy stone wall", "ivy stone wall", blocked));
        terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL,
                new DungeonTerrain("diggable ivy stone wall", "diggable ivy stone wall", "diggable ivy stone wall", blocked));
        terrainMap.put(Constants.TERRAIN_WATER,
                new DungeonTerrain("water", "water", "water 1", blocked));
        terrainMap.put(Constants.TERRAIN_LAVA,
                new DungeonTerrain("lava", "lava", "lava", blocked));

        featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, new DungeonFeature("wintile", "way to win", "orange pearl",
                new DungeonFeature.Flags(false), 0));
        featureMap.put(Constants.FEATURE_LOSE_TILE, new DungeonFeature("losetile", "death", "cobra",
                new DungeonFeature.Flags(false), 0));
        featureMap.put(Constants.FEATURE_CANDLE, new DungeonFeature("candle", "candle", "candle",
                new DungeonFeature.Flags(false), 3));
    }

    private void initBuilding() {
        terrainMap = new HashMap<>();
        DungeonTerrain.Flags open = new DungeonTerrain.Flags(false);
        DungeonTerrain.Flags blocked = new DungeonTerrain.Flags(true);
        terrainMap.put(Constants.TERRAIN_FLOOR,
                new DungeonTerrain("wood floor", "wood floor", "wood floor", open));
        terrainMap.put(Constants.TERRAIN_WALL,
                new DungeonTerrain("brown stone wall", "brown stone wall", "brown stone wall", blocked));
        terrainMap.put(Constants.TERRAIN_DIGGABLE_WALL,
                new DungeonTerrain("diggable brown stone wall", "diggable brown stone wall", "diggable brown stone wall", blocked));
        terrainMap.put(Constants.TERRAIN_WATER,
                new DungeonTerrain("water", "water", "water 1", blocked));
        terrainMap.put(Constants.TERRAIN_LAVA,
                new DungeonTerrain("lava", "lava", "lava", blocked));

        featureMap = new HashMap<>();
        featureMap.put(Constants.FEATURE_WIN_TILE, new DungeonFeature("wintile", "way to win", "orange pearl",
                new DungeonFeature.Flags(false), 0));
        featureMap.put(Constants.FEATURE_LOSE_TILE, new DungeonFeature("losetile", "death", "cobra",
                new DungeonFeature.Flags(false), 0));
        featureMap.put(Constants.FEATURE_CANDLE, new DungeonFeature("candle", "candle", "candle",
                new DungeonFeature.Flags(false), 3));
    }
}


