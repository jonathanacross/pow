package pow.backend.dungeon;

import pow.backend.ActionParams;

import java.io.Serializable;

public class DungeonFeature implements Serializable {

    public static class Flags implements Serializable {
        public boolean blockGround;
        public boolean glowing;
        public boolean actOnStep;
        public boolean stairsUp;
        public boolean stairsDown;
        public boolean openDoor;

        public Flags(boolean blockGround,
                     boolean glowing,
                     boolean actOnStep,
                     boolean stairsUp,
                     boolean stairsDown,
                     boolean openDoor) {
            this.blockGround = blockGround;
            this.glowing = glowing;
            this.actOnStep = actOnStep;
            this.stairsUp = stairsUp;
            this.stairsDown = stairsDown;
            this.openDoor = openDoor;
        }
    }

    public String id;   // program id, e.g., "axe"
    public String name; // english name, e.g., "& axe~"
    public String image; // name for display
    public Flags flags;
    public ActionParams actionParams;

    public DungeonFeature(String id, String name, String image, Flags flags, ActionParams actionParams) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.flags = flags;
        this.actionParams = actionParams;
    }
}
