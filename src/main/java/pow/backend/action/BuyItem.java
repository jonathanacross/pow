package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.ShopData;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class BuyItem implements Action {

    private final List<ShopData.ShopEntry> shopEntries;
    private final int idx;
    private final int count;

    public BuyItem(List<ShopData.ShopEntry> shopEntries, int idx, int count) {
        this.shopEntries = shopEntries;
        this.idx = idx;
        this.count = count;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        Player player = backend.getGameState().party.player;
        ShopData.ShopEntry entry = shopEntries.get(idx);

        // sanity checks
        int totalCost = entry.price * count;
        if (player.gold < totalCost) {
            backend.logMessage(player.getNoun() + " does not have enough money.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.failed();
        }
        DungeonItem item = entry.item;
        int numCanGet = Math.min(player.inventory.numCanAdd(item), item.count);
        if (numCanGet <= 0) {
            backend.logMessage(player.getNoun() + " can't hold any more.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.failed();
        }

        player.gold -= totalCost;
        if (count == item.count) {
            // if can pick up all, then just transfer the item to inventory
            player.inventory.add(item);
            shopEntries.remove(idx);
        } else {
            // if can just pick up some, then have to clone object, and update counts
            DungeonItem cloneForInventory = new DungeonItem(item);
            cloneForInventory.count = count;
            item.count -= count;
            player.inventory.add(cloneForInventory);
        }
        backend.logMessage(player.getNoun() + " buys "
                        + TextUtils.formatWithBonus(item.name, item.bonusString(), count, true),
                MessageLog.MessageType.GENERAL);

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DUNGEON_UPDATED);
        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public Actor getActor() {
        return null;
    }
}
