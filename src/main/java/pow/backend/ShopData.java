package pow.backend;

import pow.backend.dungeon.DungeonItem;

import java.io.Serializable;
import java.util.List;

public class ShopData implements Serializable {
    public enum ShopState {
        NONE,
        INN,
        WEAPON_SHOP,
        MAGIC_SHOP;

        public static ShopState parseFromString(String s) {
            switch (s.toLowerCase()) {
                case "inn": return INN;
                case "weaponshop": return WEAPON_SHOP;
                case "magicshop": return MAGIC_SHOP;
                default: return NONE;
            }
        }
    }

    public static class ShopEntry implements Serializable {
        public final DungeonItem item;
        public final int price;  // per item

        public ShopEntry(DungeonItem item, int price) {
            this.item = item;
            this.price = price;
        }
    }

    public ShopState state;
    public final int innCost;
    public final List<ShopEntry> weaponItems;
    public final List<ShopEntry> magicItems;

    public ShopData(int innCost, List<ShopEntry> weaponItems, List<ShopEntry> magicItems) {
        this.state = ShopState.NONE;
        this.innCost = innCost;
        this.weaponItems = weaponItems;
        this.magicItems = magicItems;
    }
}

