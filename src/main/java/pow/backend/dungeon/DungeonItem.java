package pow.backend.dungeon;

import pow.backend.ActionParams;
import pow.util.DieRoll;

public class DungeonItem {

    // TODO: does genMultiple belong here?
    public static class Flags {
//        public boolean genMultiple;
        public boolean potion;
//        public boolean money;
//        public boolean arrow;

        public Flags(
//                boolean genMultiple,
                boolean potion) {
//                boolean money,
//                boolean arrow) {
//            this.genMultiple = genMultiple;
            this.potion = potion;
//            this.money = money;
//            this.arrow = arrow;
        }
    }

    public enum Slot {
        NONE,
        WEAPON,
        BOW,
        SHIELD,
        HEADGEAR,
        ARMOR,
        CLOAK,
        RING,
        GLOVES,
        BOOTS;
    }

    // from dungeonobject, and there's a structure for them
//    public String id;   // program id, e.g., "axe"
    public String name; // english name, e.g., "& axe~"
    public String image; // for display
    public String description;
//    public Point loc;
//    public boolean solid;

    public Slot slot;

    public DieRoll attack;
    public int attackBonus;

    public int defense;
    public int defenseBonus;

    public int count;  // e.g. for 2 gold coins, or 23 arrows

    public ActionParams actionParams;

    public DungeonItem(String name, String image, String description, Slot slot, DieRoll attack, int attackBonus, int defense, int defenseBonus, int count, ActionParams actionParams) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.slot = slot;
        this.attack = attack;
        this.attackBonus = attackBonus;
        this.defense = defense;
        this.defenseBonus = defenseBonus;
        this.count = count;
        this.actionParams = actionParams;
    }
}
