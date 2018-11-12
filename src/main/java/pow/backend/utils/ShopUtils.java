package pow.backend.utils;

import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;

import java.util.ArrayList;
import java.util.List;

public class ShopUtils {

    public static class ItemInfo {
        public final int listIndex;  // which position in equipment/inventory list
        public final DungeonItem item;

        public ItemInfo(int listIndex, DungeonItem item) {
            this.listIndex = listIndex;
            this.item = item;
        }
    }

    public static List<ItemInfo> getListOfUpgradeableItems(List<DungeonItem> items) {
        List<ItemInfo> itemInfoList = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            DungeonItem item = items.get(i);
            if (item.bonuses[DungeonItem.SOCKETS_IDX] > 0) {
                itemInfoList.add(new ItemInfo(i, item));
            }
        }

        return itemInfoList;
    }

    public static List<ItemInfo> getListOfGems(Player player) {
        List<ItemInfo> itemInfoList = new ArrayList<>();

        for (int i = 0; i < player.inventory.items.size(); i++) {
            DungeonItem item = player.inventory.items.get(i);
            if (item.flags.gem) {
                itemInfoList.add(new ItemInfo(i, item));
            }
        }

        return itemInfoList;
    }

}
