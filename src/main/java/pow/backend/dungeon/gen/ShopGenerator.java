package pow.backend.dungeon.gen;

import pow.backend.ShopData;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopGenerator {

    private static boolean isWeapon(DungeonItem item) {
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

    private static boolean isMagical(DungeonItem item) {
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

    private static double actionBonus(DungeonItem item) {
        double bonus;
        int actionValue = item.actionParams.number;
        switch (item.actionParams.actionName) {
            case MODIFY_SPEED_ACTION: bonus = expFit(actionValue, 1, 10, 3, 1000); break;
            case HEAL_ACTION: bonus = linFit(actionValue, 10, 8, 160, 800); break;
            case RESTORE_MANA_ACTION: bonus = linFit(actionValue, 10, 6, 160, 600); break;
            case RESTORE_ACTION: bonus = linFit(actionValue, 15, 24, 240, 2400); break;
            case HEROISM_ACTION: bonus = expFit(actionValue, 8, 25, 16, 2500); break;
            case AGILITY_ACTION: bonus = expFit(actionValue, 8, 30, 16, 3000); break;
            default: bonus = 0; break;
        }
        return bonus;
    }

    private static int priceItem(DungeonItem item) {
        // Very arbitrary now.  Have to balance this.
        // While arbitrary, scalars here are fractional so that prices of
        // items don't all look the same.
        double price =
            1.6 * sqr(item.attack.die * (item.attack.roll + 1)) +
            3.4 * sqr(item.defense) +
            2.8 * sqr(item.bonuses[DungeonItem.TO_HIT_IDX]) +
            3.1 * sqr(item.bonuses[DungeonItem.TO_DAM_IDX]) +
            3.5 * sqr(item.bonuses[DungeonItem.DEF_IDX]) +
            2.1 * sqr(item.bonuses[DungeonItem.STR_IDX]) +
            3.3 * sqr(item.bonuses[DungeonItem.DEX_IDX]) +
            1.7 * sqr(item.bonuses[DungeonItem.INT_IDX]) +
            2.3 * sqr(item.bonuses[DungeonItem.CON_IDX]) +
            1000 * sqr(item.bonuses[DungeonItem.SPEED_IDX]) +
            20 * sqr(item.bonuses[DungeonItem.WEALTH_IDX]) +
            1 * (item.flags.arrow ? 1 : 0) +
            actionBonus(item);

        double slotScaleFactor;
        switch (item.slot) {
            case BOW: slotScaleFactor = 3.2; break;  // bows are relatively more useful than other weapons
            case WEAPON: slotScaleFactor = 1.0; break;
            case RING: slotScaleFactor = 1.1; break;
            case BOOTS: slotScaleFactor = 0.9; break;
            case GLOVES: slotScaleFactor = 0.8; break;
            case HEADGEAR: slotScaleFactor = 0.9; break;
            case SHIELD: slotScaleFactor = 1.0; break;
            case ARMOR: slotScaleFactor = 1.0; break;
            default: slotScaleFactor = 1.0; break;
        }
        price *= slotScaleFactor;

        if (price <= 0) {
            price = 99999; // setting price to this should be a hint that something was amiss.
            System.out.println("Warning: " + TextUtils.format(item.name, 1, true) +
                    " is not priced correctly in stores");
        }
        return (int) Math.round(price);
    }

    // generates random shop data based on the level
    public static ShopData genShop(int level, Random rng) {
        int innCost = 5 * (int) Math.round(Math.pow(1.1, level));

        // 13 chosen just to fit the current window size.. this is easily changeable
        ItemList weaponItemList = new ItemList(13, 200);
        ItemList magicItemList = new ItemList(13, 200);

        List<String> itemIds = ItemGenerator.getItemIdsForLevel(level);
        for (int i = 0; i < 40; i++) {
            int rLevel = level + rng.nextInt(10);
            DungeonItem item = ItemGenerator.genItem(itemIds.get(rng.nextInt(itemIds.size())), rLevel, rng);
            if (isWeapon(item)) {
                if (weaponItemList.numCanAdd(item) > 0) {
                    weaponItemList.add(item);
                }
            } else if (isMagical(item)) {
                if (magicItemList.numCanAdd(item) > 0) {
                    magicItemList.add(item);
                }
            }
        }

        List<ShopData.ShopEntry> weaponItems = new ArrayList<>();
        for (DungeonItem item : weaponItemList.items) {
            weaponItems.add(new ShopData.ShopEntry(item, priceItem(item)));
        }
        List<ShopData.ShopEntry> magicItems = new ArrayList<>();
        for (DungeonItem item : magicItemList.items) {
            magicItems.add(new ShopData.ShopEntry(item, priceItem(item)));
        }

        return new ShopData(innCost, weaponItems, magicItems);
    }
}
