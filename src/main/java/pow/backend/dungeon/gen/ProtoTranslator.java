package pow.backend.dungeon.gen;

import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;

import java.util.Map;

public class ProtoTranslator {

    // TODO: see if it's possible to handle just maps of integer -> string?
    private final Map<Integer, DungeonTerrain> terrainMap;
    private final Map<Integer, DungeonFeature> featureMap;

    public DungeonTerrain getTerrain(int x) { return terrainMap.get(Constants.getTerrain(x)); }
    public DungeonFeature getFeature(int x) { return featureMap.get(Constants.getFeature(x)); }

    public ProtoTranslator(Map<Integer, DungeonTerrain> terrainMap, Map<Integer, DungeonFeature> featureMap) {
        this.terrainMap = terrainMap;
        this.featureMap = featureMap;
    }
}


