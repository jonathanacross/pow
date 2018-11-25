package pow.backend.dungeon;

import pow.backend.ActionParams;

import java.io.Serializable;

public class DungeonFeature implements Serializable {

    public static class Flags implements Serializable {
        public final boolean blockGround;
        public final boolean blockWater;
        public final boolean blockAir;
        public final boolean glowing;
        public final boolean actOnStep;
        public final boolean stairsUp;
        public final boolean stairsDown;
        public final boolean trap;
        public final boolean openDoor;
        public final boolean interesting;

        public Flags(boolean blockGround,
                     boolean blockWater,
                     boolean blockAir,
                     boolean glowing,
                     boolean actOnStep,
                     boolean stairsUp,
                     boolean stairsDown,
                     boolean trap,
                     boolean openDoor,
                     boolean interesting) {
            this.blockGround = blockGround;
            this.blockWater = blockWater;
            this.blockAir = blockAir;
            this.glowing = glowing;
            this.actOnStep = actOnStep;
            // TODO: merge stairsUp and stairsDown
            this.stairsUp = stairsUp;
            this.stairsDown = stairsDown;
            this.trap = trap;
            this.openDoor = openDoor;
            this.interesting = interesting;
        }
    }

    public final String id;   // program id, e.g., "axe"
    public final String name; // english name, e.g., "& axe~"
    public final String image; // name for display
    public final Flags flags;
    public final ActionParams actionParams;

    public DungeonFeature(String id, String name, String image, Flags flags, ActionParams actionParams) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.flags = flags;
        this.actionParams = actionParams;
    }
}
