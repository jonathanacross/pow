package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonFeature implements Serializable {

    public static class Flags implements Serializable {
        public boolean blockGround;

        public Flags(boolean blockGround) {
            this.blockGround = blockGround;
        }
    }

    public String id;   // program id, e.g., "axe"
    public String name; // english name, e.g., "& axe~"
    public Flags flags;

    public DungeonFeature(String id, String name, Flags flags) {
        this.id = id;
        this.name = name;
        this.flags = flags;
    }
}
