package pow.backend.dungeon.gen;

import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;

import java.util.Map;

public class ProtoTranslator {

    // TODO: see if it's possible to handle just maps of integer -> string?
    private final Map<Integer, DungeonTerrain> terrainMap;
    private final Map<Integer, DungeonFeature> featureMap;

    public DungeonTerrain getTerrain(int x) {
        int terrainValue = Constants.getTerrain(x);
        if (!terrainMap.containsKey(terrainValue)) {
            throw new RuntimeException("couldn't translate terrain value " + terrainValue);
        }
        return terrainMap.get(terrainValue);
    }
    public DungeonFeature getFeature(int x) {
        int featureValue = Constants.getFeature(x);
        if (!featureMap.containsKey(featureValue)) {
            throw new RuntimeException("couldn't translate feature value " + featureValue);
        }
        return featureMap.get(featureValue);
    }

    public ProtoTranslator(Map<Integer, DungeonTerrain> terrainMap, Map<Integer, DungeonFeature> featureMap) {
        this.terrainMap = terrainMap;
        this.featureMap = featureMap;
    }
}


