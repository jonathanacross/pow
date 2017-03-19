package pow.backend.dungeon;

import pow.backend.ActionParams;

import java.io.Serializable;

public class DungeonTerrain implements Serializable {

    public static class Flags implements Serializable {
        public final boolean blockGround;
        public final boolean blockWater;
        public final boolean blockAir;
        public final boolean diggable;
        public final boolean actOnStep;
        public final boolean teleport;  // teleports you to another area

        public Flags(boolean blockGround,
                     boolean blockWater,
                     boolean blockAir,
                     boolean diggable,
                     boolean actOnStep,
                     boolean teleport) {
            this.blockGround = blockGround;
            this.blockWater = blockWater;
            this.blockAir = blockAir;
            this.diggable = diggable;
            this.actOnStep = actOnStep;
            this.teleport = teleport;
        }
    }

    public final String id;
    public final String image;
    public final String name;
    public final Flags flags;
    public final ActionParams actionParams;

    public DungeonTerrain(String id, String name, String image, Flags flags, ActionParams actionParams) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.flags = flags;
        this.actionParams = actionParams;
    }
}
