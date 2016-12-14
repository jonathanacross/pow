package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonTerrain implements Serializable {

    public static class Flags implements Serializable {
        public boolean blockGround;

        public Flags(boolean blockGround) {
            this.blockGround = blockGround;
        }
    }

    public String id;
    public String image;
    public String name;
    public Flags flags;

    public DungeonTerrain(String id, String name, String image, Flags flags) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.flags = flags;
    }
}
