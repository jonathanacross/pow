package pow.backend;

import java.io.Serializable;

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

    public ShopState state;

    public ShopData() {
        this.state = ShopState.NONE;
    }
}

