package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonSquare implements Serializable {
    public DungeonTerrain terrain;
    public DungeonFeature feature;
    public final ItemList items;

    // These hold information for player visibility; must be updated every time
    // the player moves.

    // Has the player seen the square?
    public boolean seen;

    // How bright is the square -- recomputed based on light sources in the dungeon
    // (currently only depending on the player and static dungeon features).
    // See comments in GameMap.updateBrightness for how this is used.
    public int brightness;

    public DungeonSquare(DungeonTerrain terrain, DungeonFeature feature) {
        this.terrain = terrain;
        this.feature = feature;
        this.items = new ItemList();
    }

    public boolean blockGround() {
        return terrain.flags.blockGround || (feature != null && feature.flags.blockGround);
    }

    public boolean blockWater() {
        return terrain.flags.blockWater || (feature != null && feature.flags.blockWater);
    }

    public boolean blockAir() {
        return terrain.flags.blockAir || (feature != null && feature.flags.blockAir);
    }

}
