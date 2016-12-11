package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonSquare implements Serializable {
    public DungeonTerrain terrain;
    public DungeonFeature feature;
    // add list of items here, too

    public DungeonSquare(DungeonTerrain terrain, DungeonFeature feature) {
        this.terrain = terrain;
        this.feature = feature;
    }

    public boolean blockGround() {
        return terrain.flags.blockGround || (feature != null && feature.flags.blockGround);
    }

}
