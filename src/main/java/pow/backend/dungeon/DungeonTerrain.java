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
    public String name;
    public Flags flags;

    public DungeonTerrain(String id, String name, Flags flags) {
        this.id = id;
        this.name = name;
        this.flags = flags;
    }
}
