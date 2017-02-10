package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonSquare implements Serializable {
    public DungeonTerrain terrain;
    public DungeonFeature feature;
    public ItemList items;

    // These hold information for player visibility; must be updated every time
    // the player moves.

    // Has the player seen the square?
    public boolean seen;

    // How bright is the square -- recomputed based on lightsources in the dungeon
    // (currently only depending on the player and static dungeon features).
    public int brightness;

    // Is the square is illuminated or not? squares will be lit on easy dungeons,
    // if outside, or if the player has cast a spell to light an area
    public boolean illuminated;

    public DungeonSquare(DungeonTerrain terrain, DungeonFeature feature) {
        this.terrain = terrain;
        this.feature = feature;
        this.items = new ItemList();
    }

    public boolean blockGround() {
        return terrain.flags.blockGround || (feature != null && feature.flags.blockGround);
    }

    public boolean blockAir() {
        return terrain.flags.blockAir || (feature != null && feature.flags.blockAir);
    }

}
