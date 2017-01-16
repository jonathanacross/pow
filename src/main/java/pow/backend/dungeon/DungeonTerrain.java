package pow.backend.dungeon;

import pow.backend.ActionParams;

import java.io.Serializable;

public class DungeonTerrain implements Serializable {

    public static class Flags implements Serializable {
        public boolean blockGround;
        public boolean diggable;
        public boolean actOnStep;
        public boolean teleport;  // teleports you to another area

        public Flags(boolean blockGround, boolean diggable, boolean actOnStep, boolean teleport) {
            this.blockGround = blockGround;
            this.diggable = diggable;
            this.actOnStep = actOnStep;
            this.teleport = teleport;
        }
    }

    public String id;
    public String image;
    public String name;
    public Flags flags;
    public ActionParams actionParams;

    public DungeonTerrain(String id, String name, String image, Flags flags, ActionParams actionParams) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.flags = flags;
        this.actionParams = actionParams;
    }
}
