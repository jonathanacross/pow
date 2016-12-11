package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonFeature implements Serializable {

    public static class Flags implements Serializable {
        public boolean blockGround;

        public Flags(boolean blockGround) {
            this.blockGround = blockGround;
        }
    }

    public String name;
    public String image;
    public Flags flags;

    public DungeonFeature(String name, String image, Flags flags) {
        this.name = name;
        this.image = image;
        this.flags = flags;
    }
}
