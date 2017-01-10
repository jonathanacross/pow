package pow.backend.dungeon;

import pow.backend.ActionParams;

import javax.swing.Action;
import java.io.Serializable;

public class DungeonFeature implements Serializable {

    public static class Flags implements Serializable {
        public boolean blockGround;
        public boolean glowing;
        public boolean actOnStep;

        public Flags(boolean blockGround, boolean glowing, boolean actOnStep) {
            this.blockGround = blockGround;
            this.glowing = glowing;
            this.actOnStep = actOnStep;
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
