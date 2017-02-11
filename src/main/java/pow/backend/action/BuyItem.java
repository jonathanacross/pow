package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class BuyItem implements Action {

    private List<ShopData.ShopEntry> shopEntries;
    private int idx;
    private int count;

    public BuyItem(List<ShopData.ShopEntry> shopEntries, int idx, int count) {
        this.shopEntries = shopEntries;
        this.idx = idx;
        this.count = count;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        Player player = backend.getGameState().player;
        ShopData.ShopEntry entry = shopEntries.get(idx);

        // sanity checks
        int totalCost = entry.price * count;
        if (player.gold < totalCost) {
            backend.logMessage("You do not have enough money.");
            return ActionResult.Failed(null);
        }
        DungeonItem item = entry.item;
        int numCanGet = Math.min(player.inventory.numCanAdd(item), item.count);
        if (numCanGet <= 0) {
            backend.logMessage(player.getPronoun() + " can't hold any more.");
            return ActionResult.Failed(null);
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
        backend.logMessage(player.getPronoun() + " buy " + TextUtils.format(item.name, count, true));

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
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
