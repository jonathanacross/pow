package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonFeature implements Serializable {

    public static class Flags implements Serializable {
        public boolean blockGround;
        public boolean glowing;

        public Flags(boolean blockGround, boolean glowing) {
            this.blockGround = blockGround;
            this.glowing = glowing;
        }
    }

    public String id;   // program id, e.g., "axe"
    public String name; // english name, e.g., "& axe~"
    public String image; // name for display
    public Flags flags;

    public DungeonFeature(String id, String name, String image, Flags flags) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.flags = flags;
    }
}
