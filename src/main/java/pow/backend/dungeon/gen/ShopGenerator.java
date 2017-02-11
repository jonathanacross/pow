package pow.backend.dungeon.gen;

import pow.backend.ShopData;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ShopGenerator {

    // TODO: This is a simple placeholder.  Eventually read from a file or generate automatically/randomly
    public static ShopData genShop(int level, Random rng) {

        int innCost = 5;
        List<ShopData.ShopEntry> weaponItems = Arrays.asList(
                new ShopData.ShopEntry(ItemGenerator.genItem("dagger", 1, rng), 10),
                new ShopData.ShopEntry(ItemGenerator.genItem("short bow", 1, rng),15),
                new ShopData.ShopEntry(ItemGenerator.genItem("arrow", 1, rng), 1),
                new ShopData.ShopEntry(ItemGenerator.genItem("thin leather gloves", 1, rng), 5),
                new ShopData.ShopEntry(ItemGenerator.genItem("soft leather boots", 1, rng), 5),
                new ShopData.ShopEntry(ItemGenerator.genItem("cloak", 1, rng), 5),
                new ShopData.ShopEntry(ItemGenerator.genItem("small wood shield", 1, rng), 5),
                new ShopData.ShopEntry(ItemGenerator.genItem("robe", 1, rng), 7) );
        List<ShopData.ShopEntry> magicItems = Arrays.asList(
                new ShopData.ShopEntry(ItemGenerator.genItem("health potion", 1, rng), 8),
                new ShopData.ShopEntry(ItemGenerator.genItem("ring of defense", 1, rng), 5),
                new ShopData.ShopEntry(ItemGenerator.genItem("bracelet of defense", 1, rng), 5),
                new ShopData.ShopEntry(ItemGenerator.genItem("amulet of defense", 1, rng), 5) );

        return new ShopData(innCost, weaponItems, magicItems);
    }
}
