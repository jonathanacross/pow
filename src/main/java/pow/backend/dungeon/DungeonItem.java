package pow.backend.dungeon;

import pow.backend.ActionParams;
import pow.util.TextUtils;

import java.io.Serializable;
import java.util.*;

public class DungeonItem implements Comparable<DungeonItem>, Serializable {

    public static class Flags implements Serializable {
        public final boolean potion;
        public final boolean money;
        public final boolean arrow;
        public final boolean gem;

        public Flags(
                boolean potion,
                boolean money,
                boolean arrow,
                boolean gem) {
            this.potion = potion;
            this.money = money;
            this.arrow = arrow;
            this.gem = gem;
        }

        public int getSortValue() {
            return (potion ? 1 : 0) +
                    (money ? 2 : 0) +
                    (arrow ? 4 : 0) +
                    (gem ? 8 : 0);
        }

    }

    public static final int TO_HIT_IDX = 0;
    public static final int TO_DAM_IDX = 1;
    public static final int DEF_IDX = 2;
    public static final int STR_IDX = 3;
    public static final int DEX_IDX = 4;
    public static final int INT_IDX = 5;
    public static final int CON_IDX = 6;
    public static final int RES_FIRE_IDX = 7;
    public static final int RES_COLD_IDX = 8;
    public static final int RES_ACID_IDX = 9;
    public static final int RES_ELEC_IDX = 10;
    public static final int RES_POIS_IDX = 11;
    public static final int SPEED_IDX = 12;
    public static final int WEALTH_IDX = 13;
    public static final int SOCKETS_IDX = 14;
    public static final int NUM_BONUSES = 15;

    private static final String[] bonusNames = {
            "hit",
            "dam",
            "def",
            "str",
            "dex",
            "int",
            "con",
            "rFire",
            "rCold",
            "rAcid",
            "rElec",
            "rPois",
            "speed",
            "wealth",
            "socket~"
    };

    public enum Slot {
        NONE,
        WEAPON,
        BOW,
        SHIELD,
        HEADGEAR,
        ARMOR,
        CLOAK,
        RING,
        BRACELET,
        AMULET,
        GLOVES,
        BOOTS
    }

    public enum ArtifactSlot {
        NONE,
        PEARL1,
        PEARL2,
        PEARL3,
        PEARL4,
        PEARL5,
        PEARL6,
        PEARL7,
        PEARL8,
        LANTERN,
        LANTERN2,
        KEY,
        FLOAT,
        GASMASK,
        GLASSES,
        PICKAXE,
        HEATSUIT,
        PETSTATUE,
        BAG,
        PORTALKEY,
        XRAYSCOPE,
        MAP
    }

    public final String id; // for internal reference
    public final String name; // english name, e.g., "& axe~"
    public final String image; // for display
    public final String description;
    public final Flags flags;
    public final Slot slot;
    public final ArtifactSlot artifactSlot;
    public final int[] bonuses;

    public int count;  // e.g. for 2 gold coins, or 23 arrows

    public final ActionParams actionParams;

    public DungeonItem(String id,
                       String name,
                       String image,
                       String description,
                       Slot slot,
                       ArtifactSlot artifactSlot,
                       Flags flags,
                       int[] bonuses,
                       int count,
                       ActionParams actionParams) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.slot = slot;
        this.artifactSlot = artifactSlot;
        this.flags = flags;
        this.bonuses = bonuses;
        this.count = count;
        this.actionParams = actionParams;
    }

    // copy/clone constructor
    public DungeonItem(DungeonItem other) {
        this.id = other.id;
        this.name = other.name;
        this.image = other.image;
        this.description = other.description;
        this.slot = other.slot;
        this.artifactSlot = other.artifactSlot;
        this.flags = other.flags;
        this.bonuses = Arrays.copyOf(other.bonuses, NUM_BONUSES);
        this.count = other.count;
        this.actionParams = other.actionParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DungeonItem that = (DungeonItem) o;

        if (!name.equals(that.name)) return false;
        return Arrays.equals(bonuses, that.bonuses);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(bonuses);
        return result;
    }

    @Override
    public int compareTo(DungeonItem other) {
        // first order by slot
        int i = slot.compareTo(other.slot);
        if (i != 0) return i;

        // within slot, sort by flags; this orders potions/gems/arrows.
        i = Integer.compare(flags.getSortValue(), other.flags.getSortValue());
        if (i != 0) return i;

        // also within slot, sort by decreasing total bonus
        int thisTotalBonus = 0;
        int otherTotalBonus = 0;
        for (int b = 0; b < NUM_BONUSES; b++) {
            thisTotalBonus += bonuses[b];
            otherTotalBonus += other.bonuses[b];
        }
        i = Integer.compare(otherTotalBonus, thisTotalBonus);
        if (i != 0) return i;

        // if bonuses are the same, then sort by name, then by bonuses
        i = id.compareTo(other.id);
        if (i != 0) return i;

        i = name.compareTo(other.name);
        if (i != 0) return i;

        for (int b = 0; b < NUM_BONUSES; b++) {
            i = Integer.compare(bonuses[b], other.bonuses[b]);
            if (i != 0) return i;
        }

        i = artifactSlot.compareTo(other.artifactSlot);
        if (i != 0) return i;

        return 0;
    }

    private static String formatBonus(int x) {
        if (x < 0) { return "-" + (-x); }
        else { return "+" + x; }
    }

    public String bonusString() {
        return formatGroupBonus(bonuses, bonusNames);
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
                stats.add(TextUtils.formatThing(names[idx], bonusAmt > 1));
            }
            groups.add(formatBonus(bonusAmt) + " " + String.join("/", stats));
        }
        return "(" + String.join(", ", groups) + ")";
    }

    // Formats an item in a nice way, showing all the stats
    public String stringWithInfo() {
        return TextUtils.format(name, count, false) + " " +
                formatGroupBonus(bonuses, bonusNames);
    }
}
