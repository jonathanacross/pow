package pow.backend.utils;

import pow.backend.dungeon.DungeonItem;
import pow.util.TextUtils;

public class ItemUtils {
    public static boolean isWeapon(DungeonItem item) {
        switch (item.slot) {
            case AMULET:
            case RING:
            case BRACELET:
                return false;
            case WEAPON:
            case BOW:
            case SHIELD:
            case HEADGEAR:
            case ARMOR:
            case CLOAK:
            case GLOVES:
            case BOOTS:
                return true;
            case NONE:
                return item.flags.arrow;
        }

        throw new RuntimeException("shouldn't get here.");
    }

    public static boolean isMagical(DungeonItem item) {
        switch (item.slot) {
            case AMULET:
            case RING:
            case BRACELET:
                return true;
            case WEAPON:
            case BOW:
            case SHIELD:
            case HEADGEAR:
            case ARMOR:
            case CLOAK:
            case GLOVES:
            case BOOTS:
                return false;
            case NONE:
                return item.flags.potion;
        }
        throw new RuntimeException("shouldn't get here.");
    }

    // --------------- functions related to item pricing -------

    private static double sqr(int x) {
        return x*x;
    }

    // Returns an exponential price for x, assuming
    // that the price for value x1 is y1, and the price for
    // value x2 is y2.  Model is y = a e^(bx).
    private static double expFit(int x, int x1, int y1, int x2, int y2) {
        double b = (Math.log(y2) - Math.log(y1)) / (x2 - x1);
        double a = y1 / Math.exp(b * x1);
        return a * Math.exp(b * x);
    }

    // Fits the model y = ax + b, and evaluates y(x).
    private static double linFit(int x, int x1, int y1, int x2, int y2) {
        double a = (double) (y2 - y1) / (x2 - x1);
        return a * (x - x1) + y1;
    }

    private static double actionCost(DungeonItem item) {
        double bonus;
        int actionValue = item.actionParams.number;
        switch (item.actionParams.actionName) {
            case MODIFY_SPEED_ACTION: bonus = expFit(actionValue, 1, 10, 3, 1000); break;
            case HEAL_ACTION: bonus = linFit(actionValue, 20, 8, 320, 800); break;
            case RESTORE_MANA_ACTION: bonus = linFit(actionValue, 15, 6, 240, 600); break;
            case RESTORE_ACTION: bonus = linFit(actionValue, 30, 24, 480, 2400); break;
            case HEROISM_ACTION: bonus = expFit(actionValue, 8, 25, 16, 2500); break;
            case AGILITY_ACTION: bonus = expFit(actionValue, 8, 30, 16, 3000); break;
            default: bonus = 0; break;
        }
        return bonus;
    }

    private static double slotScaleFactor(DungeonItem item) {
        switch (item.slot) {
            case BOW: return 1.8;  // bows are relatively more useful than melee weapons
            case WEAPON: return 1.0;
            case RING: return 1.0;
            case AMULET: return 1.0;
            case BRACELET: return 1.0;
            case BOOTS: return 0.9;
            case GLOVES: return 0.8;
            case HEADGEAR: return 0.9;
            case SHIELD: return 1.0;
            case ARMOR: return 1.0;
            default: return 1.0;
        }
    }

    private static double bonusCost(DungeonItem item) {
        // These are somewhat ad-hoc.  Note that we use slightly different
        // constants to give some slight variability between item prices.
        double totalWeightedBonus =
                1.58 * item.bonuses[DungeonItem.TO_HIT_IDX] +
                1.67 * item.bonuses[DungeonItem.TO_DAM_IDX] +
                2.40 * item.bonuses[DungeonItem.DEF_IDX] +
                7.77 * item.bonuses[DungeonItem.STR_IDX] +
                9.70 * item.bonuses[DungeonItem.DEX_IDX] +
                7.00 * item.bonuses[DungeonItem.INT_IDX] +
                8.14 * item.bonuses[DungeonItem.CON_IDX] +
                8.42 * item.bonuses[DungeonItem.RES_FIRE_IDX] +
                8.00 * item.bonuses[DungeonItem.RES_COLD_IDX] +
                7.80 * item.bonuses[DungeonItem.RES_ACID_IDX] +
                7.58 * item.bonuses[DungeonItem.RES_ELEC_IDX] +
                8.58 * item.bonuses[DungeonItem.RES_POIS_IDX] +
                8.85 * item.bonuses[DungeonItem.RES_DAM_IDX] +
                31.62 * item.bonuses[DungeonItem.SPEED_IDX] +
                4.47 * item.bonuses[DungeonItem.WEALTH_IDX] +
                10.00 * item.bonuses[DungeonItem.SOCKETS_IDX];
        return totalWeightedBonus * totalWeightedBonus;
    }

    public static int priceItem(DungeonItem item) {
        double price =
                slotScaleFactor(item) * bonusCost(item) + // price for equipment and gems
                actionCost(item) + // price for potions
                (item.flags.arrow ? 1 : 0) +  // arrows
                (item.flags.money ? 1 : 0); // money (for completeness, not that you would ever buy it)


        if (price <= 0) {
            price = 99999; // setting price to this should be a hint that something was amiss.
            System.out.println("Warning: " + TextUtils.format(item.name, 1, true) +
                    " is not priced correctly in stores");
        }
        return (int) Math.round(price);
    }
}
