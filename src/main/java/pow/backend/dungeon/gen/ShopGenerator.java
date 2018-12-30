package pow.backend.dungeon.gen;

import pow.backend.ShopData;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;
import pow.backend.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopGenerator {

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
            if (ItemUtils.isWeapon(item)) {
                if (weaponItemList.numCanAdd(item) > 0) {
                    weaponItemList.add(item);
                }
            } else if (ItemUtils.isMagical(item)) {
                if (magicItemList.numCanAdd(item) > 0) {
                    magicItemList.add(item);
                }
            }
        }

        List<ShopData.ShopEntry> weaponItems = new ArrayList<>();
        for (DungeonItem item : weaponItemList.items) {
            weaponItems.add(new ShopData.ShopEntry(item, ItemUtils.priceItem(item)));
        }
        List<ShopData.ShopEntry> magicItems = new ArrayList<>();
        for (DungeonItem item : magicItemList.items) {
            magicItems.add(new ShopData.ShopEntry(item, ItemUtils.priceItem(item)));
        }

        return new ShopData(innCost, weaponItems, magicItems);
    }
}
