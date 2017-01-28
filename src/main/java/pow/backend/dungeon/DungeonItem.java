package pow.backend.dungeon;

import pow.backend.ActionParams;
import pow.util.DieRoll;
import pow.util.TextUtils;

import java.io.Serializable;
import java.util.*;

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

    public static final int TO_HIT_IDX = 0;
    public static final int TO_DAM_IDX = 1;
    public static final int DEF_IDX = 2;
    public static final int STR_IDX = 3;
    public static final int DEX_IDX = 4;
    public static final int INT_IDX = 5;
    public static final int CON_IDX = 6;
    public static final int SPEED_IDX = 7;
    public static final int NUM_BONUSES = 8;

    public enum Slot {
        NONE,
        WEAPON,
        BOW,
        SHIELD,
        HEADGEAR,
        ARMOR,
        CLOAK,
        RING,
        AMULET,
        GLOVES,
        BOOTS;
    }

    public String name; // english name, e.g., "& axe~"
    public String image; // for display
    public String description;
    public Flags flags;
    public Slot slot;
    public DieRoll attack;
    public int defense;
    public int[] bonuses;

    public int count;  // e.g. for 2 gold coins, or 23 arrows

    public ActionParams actionParams;

    public DungeonItem(String name,
                       String image,
                       String description,
                       Slot slot,
                       Flags flags,
                       int[] bonuses,
                       DieRoll attack,
                       int defense,
                       int count,
                       ActionParams actionParams) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.slot = slot;
        this.flags = flags;
        this.bonuses = bonuses;
        this.attack = attack;
        this.defense = defense;
        this.count = count;
        this.actionParams = actionParams;
    }

    // copy/clone constructor
    public DungeonItem(DungeonItem other) {
        this.name = other.name;
        this.image = other.image;
        this.description = other.description;
        this.slot = other.slot;
        this.flags = other.flags;
        this.attack = other.attack;
        this.defense = other.defense;
        this.bonuses = other.bonuses;
        this.count = other.count;
        this.actionParams = other.actionParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DungeonItem that = (DungeonItem) o;

        if (count != that.count) return false;
        if (!name.equals(that.name)) return false;
        return Arrays.equals(bonuses, that.bonuses);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(bonuses);
        result = 31 * result + count;
        return result;
    }

    @Override
    public int compareTo(DungeonItem other) {
        int i = name.compareTo(other.name);
        if (i != 0) return i;

        i = slot.compareTo(other.slot);
        if (i != 0) return i;

        int thisAttackValue = attack != null ? (attack.die * 10000) + (attack.roll * 100) + attack.plus : -1;
        int otherAttackValue = other.attack != null ? (other.attack.die * 10000) + (other.attack.roll * 100) + other.attack.plus : -1;
        i = Integer.compare(thisAttackValue, otherAttackValue);
        if (i != 0) return i;

        i = Integer.compare(defense, other.defense);
        if (i != 0) return i;

        for (int b = 0; b < NUM_BONUSES; b++) {
            i = Integer.compare(bonuses[b], other.bonuses[b]);
            if (i != 0) return i;
        }

        return 0;
    }

    private static String formatBonus(int x) {
        if (x < 0) { return "-" + (-x); }
        else { return "+" + x; }
    }

    private static String formatGroupBonus(int[] bonusAmts, String[] names) {
        // simple case - see if all 0
        int numNonZero = 0;
        for (int bonusAmt : bonusAmts) {
            if (bonusAmt != 0) numNonZero++;
        }
        if (numNonZero == 0) {
            return "";
        }

        // for all nonzero bonuses, group by the amount
        SortedMap<Integer, List<Integer>> bonusAmtToIdx = new TreeMap<>();
        for (int i = 0; i < bonusAmts.length; i++) {
            int bonus = bonusAmts[i];
            if (bonus == 0) continue;

            if (!bonusAmtToIdx.containsKey(bonus)) {
                bonusAmtToIdx.put(bonus, new ArrayList<>());
            }
            bonusAmtToIdx.get(bonus).add(i);
        }
        List<String> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : bonusAmtToIdx.entrySet()) {
            int bonusAmt = entry.getKey();
            List<Integer> statIdxs = entry.getValue();

            List<String> stats = new ArrayList<>();
            for (int idx : statIdxs) {
                stats.add(names[idx]);
            }
            groups.add(formatBonus(bonusAmt) + " to " + String.join("/", stats));
        }
        return String.join(", ", groups);
    }

    // Formats an item in a nice way, showing all the stats
    public String stringWithInfo() {
        StringBuilder sb = new StringBuilder();

        sb.append(TextUtils.format(name, count, false));

        if (attack != null && (attack.die + attack.plus + attack.roll > 0)) {
            sb.append(" (" + attack.toString() + ")");
            sb.append(" (" + formatBonus(bonuses[TO_HIT_IDX]) + "," + formatBonus(bonuses[TO_DAM_IDX]) + ")");
        } else if ((bonuses[TO_HIT_IDX] != 0) || (bonuses[TO_DAM_IDX] != 0)) {
            // this happens for rings of attack, where there is no inherent damage, just bonuses
            sb.append(" (" + formatBonus(bonuses[TO_HIT_IDX]) + "," + formatBonus(bonuses[TO_DAM_IDX]) + ")");
        }

        if (defense > 0) {
            sb.append(" [" + defense + "," + formatBonus(bonuses[DEF_IDX]) + "]");
        } else if (bonuses[DEF_IDX] != 0) {
            // happens, e.g., for rings of defense
            sb.append(" [" + formatBonus(bonuses[DEF_IDX]) + "]");
        }

        if ((bonuses[STR_IDX] != 0) || (bonuses[DEX_IDX] != 0) ||
                (bonuses[INT_IDX] != 0) || (bonuses[CON_IDX] != 0) ||
                (bonuses[SPEED_IDX] != 0)) {
            sb.append(" {" + formatGroupBonus(
                    new int[] {bonuses[STR_IDX], bonuses[DEX_IDX], bonuses[INT_IDX], bonuses[CON_IDX], bonuses[SPEED_IDX]},
                    new String[] {"Str", "Dex", "Int", "Con", "Speed"}
                    ) + "}");
        }
        return sb.toString();
    }
}
