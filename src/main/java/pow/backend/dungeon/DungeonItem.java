package pow.backend.dungeon;

import pow.backend.ActionParams;
import pow.util.DieRoll;

import java.io.Serializable;

public class DungeonItem implements Comparable<DungeonItem>, Serializable {

    // TODO: does genMultiple belong here?
    public static class Flags implements Serializable {
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

    // copy/clone constructor
    public DungeonItem(DungeonItem other) {
        this.name = other.name;
        this.image = other.image;
        this.description = other.description;
        this.slot = other.slot;
        this.attack = other.attack;
        this.attackBonus = other.attackBonus;
        this.defense = other.defense;
        this.defenseBonus = other.defenseBonus;
        this.count = other.count;
        this.actionParams = other.actionParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DungeonItem that = (DungeonItem) o;

        if (!name.equals(that.name)) return false;
        if (slot != that.slot) return false;
        if (attack != null ? !attack.equals(that.attack) : that.attack != null) return false;
        if (attackBonus != that.attackBonus) return false;
        if (defense != that.defense) return false;
        if (defenseBonus != that.defenseBonus) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + slot.hashCode();
        result = 31 * result + (attack != null ? attack.hashCode() : 0);
        result = 31 * result + attackBonus;
        result = 31 * result + defense;
        result = 31 * result + defenseBonus;
        return result;
    }

    @Override
    public int compareTo(DungeonItem other) {
        int i = name.compareTo(other.name);
        if (i != 0) return i;

        i = slot.compareTo(other.slot);
        if (i != 0) return i;

        int thisAttackValue = attack != null ? (attack.die * 10000) + (attack.roll * 100) + attackBonus : -1;
        int otherAttackValue = other.attack != null ? (other.attack.die * 10000) + (other.attack.roll * 100) + other.attackBonus : -1;
        i = Integer.compare(thisAttackValue, otherAttackValue);
        if (i != 0) return i;

        i = Integer.compare(attackBonus, other.attackBonus);
        if (i != 0) return i;

        i = Integer.compare(defense, other.defense);
        if (i != 0) return i;

        i = Integer.compare(defenseBonus, other.defenseBonus);
        if (i != 0) return i;

        return 0;
    }
}
