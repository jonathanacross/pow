package pow.backend.dungeon.gen;

import pow.backend.ShopData;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;

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
            8 * (item.actionParams.actionName != null && item.actionParams.actionName.equals("heal") ? 1 : 0) +
            100 * (item.actionParams.actionName != null && item.actionParams.actionName.equals("restore") ? 1 : 0);

        double slotScaleFactor = 1.0;
        switch (item.slot) {
            case BOW: slotScaleFactor = 3.2; break;  // bows are relatively more useful than other weapons
            case WEAPON: slotScaleFactor = 1.0; break;
            case RING: slotScaleFactor = 1.1; break;
            case BOOTS: slotScaleFactor = 0.9; break;
            case GLOVES: slotScaleFactor = 0.8; break;
            case HEADGEAR: slotScaleFactor = 0.9; break;
            case SHIELD: slotScaleFactor = 1.0; break;
            case ARMOR: slotScaleFactor = 1.0; break;
        }
        price *= slotScaleFactor;

        if (price <= 0) {
            price = 99999; // setting price to this should be a hint that something was amiss.
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
