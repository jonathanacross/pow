package pow.backend.dungeon;

import java.io.Serializable;
import java.util.List;

public class DungeonSquare implements Serializable {
    public DungeonTerrain terrain;
    public DungeonFeature feature;
    public List<DungeonItem> items;

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

    // add list of items here, too

    public DungeonSquare(DungeonTerrain terrain, DungeonFeature feature) {
        this.terrain = terrain;
        this.feature = feature;
        this.items = null;  // or new arraylist?
    }

    public DungeonSquare(DungeonTerrain terrain, DungeonFeature feature, List<DungeonItem> items) {
        this.terrain = terrain;
        this.feature = feature;
        this.items = items;
    }

    public boolean blockGround() {
        return terrain.flags.blockGround || (feature != null && feature.flags.blockGround);
    }

}
