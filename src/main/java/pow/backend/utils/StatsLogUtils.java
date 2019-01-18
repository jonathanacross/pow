package pow.backend.utils;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.util.DebugLogger;
import pow.util.TsvReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class StatsLogUtils {
    private static final File STATS_DIR;
    private static final String SHOP_DATA_FILE_NAME = "shop_data.tsv";

    static {
        String home = System.getProperty("user.home");
        String sep = System.getProperty("file.separator");
        STATS_DIR = new File(home, ".pearls_of_wisdom" + sep + "stats");
    }

    private static void makeStatsDir() {
        if (!STATS_DIR.exists()) {
            if (!STATS_DIR.mkdirs()) {
                RuntimeException e = new RuntimeException("Error: could not create stats directory.  No stats will be recorded.");
                DebugLogger.fatal(e);
                throw e;
            }
        }
    }


    public static void recordItemBought(DungeonItem item, int price) {
        makeStatsDir();
        File file = new File(STATS_DIR, SHOP_DATA_FILE_NAME);
        try (
                Writer fw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            ItemCostStats stats = new ItemCostStats(item, ItemCostStats.Action.BUY, price);
            bw.write(stats.toTsv());
        } catch (IOException ex) {
            DebugLogger.fatal(ex);
        }
    }

    public static void recordItemsSeen(GameBackend backend) {
        makeStatsDir();
        File file = new File(STATS_DIR, SHOP_DATA_FILE_NAME);

        List<ShopData.ShopEntry> shopEntries = getSeenItems(backend);
        if (shopEntries.isEmpty()) {
            return;
        }

        try (
                Writer fw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            for (ShopData.ShopEntry entry : shopEntries) {
                ItemCostStats stats = new ItemCostStats(entry.item, ItemCostStats.Action.SEE, entry.price);
                bw.write(stats.toTsv());
            }
        } catch (IOException ex) {
            DebugLogger.fatal(ex);
        }
    }

    public static List<ItemCostStats> readShopData() throws IOException {
        File file = new File(STATS_DIR, SHOP_DATA_FILE_NAME);
        if (!file.exists()) {
            throw new IOException(file.getName() + "doesn't exist. No stats");
        }

        TsvReader reader = new TsvReader(file);
        List<ItemCostStats> stats = new ArrayList<>();
        for (String[] fields : reader.getData()) {
            stats.add(new ItemCostStats(fields));
        }

        return stats;
    }

    private static List<ShopData.ShopEntry> getSeenItems(GameBackend backend) {
        ShopData shopData = backend.getGameState().getCurrentMap().shopData;
        List<ShopData.ShopEntry> entries = new ArrayList<>();
        switch (shopData.state) {
            case WEAPON_SHOP:
                entries.addAll(shopData.weaponItems);
                break;
            case MAGIC_SHOP:
                entries.addAll(shopData.magicItems);
                break;
            default:
                break;
        }

        // filter down to items that the player could buy.
        Player player = backend.getGameState().party.player;
        entries.removeIf(e -> e.price > player.gold);

        // remove items that the player wouldn't want
        entries.removeIf(e -> playerProbablyDoesntWant(player, e.item));

        return entries;
    }

    // If there's an item that's
    // cheaper than an item that the player already has, then
    // the player wil probably ignore it.  Assumes that price is monotonically
    // increasing with item usefulness.
    private static boolean playerProbablyDoesntWant(Player player, DungeonItem item) {
        List<DungeonItem> playerItems = new ArrayList<>();
        playerItems.addAll(player.equipment.items);
        playerItems.addAll(player.inventory.items);
        // don't use the store price since it might be modified from the original true price.
        int trueItemPrice = ItemUtils.priceItem(item);
        for (DungeonItem pItem : playerItems) {
            if (item.slot != DungeonItem.Slot.NONE && item.slot == pItem.slot) {
                if (ItemUtils.priceItem(pItem) >= trueItemPrice) {
                    return true;
                }
            }
        }
        return false;
    }
}
