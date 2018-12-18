package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class UpgradeItem implements Action {

    public static class UpgradeInfo {
        public final int equipmentIdx;
        public final int inventoryIdx;
        public final int gemIdx;
        public final int price;

        public UpgradeInfo(int equipmentIdx, int inventoryIdx, int gemIdx, int price) {
            this.equipmentIdx = equipmentIdx;
            this.inventoryIdx = inventoryIdx;
            this.gemIdx = gemIdx;
            this.price = price;
        }
    }

    private final UpgradeInfo upgradeInfo;

    public UpgradeItem(UpgradeInfo upgradeInfo) {
        this.upgradeInfo = upgradeInfo;
    }

    private void upgradeItem(DungeonItem item, DungeonItem gem, GameBackend backend, Player player) {
        // transfer the bonuses from the gem to the item
        for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
            item.bonuses[i] += gem.bonuses[i];
        }
        // remove a socket
        item.bonuses[DungeonItem.SOCKETS_IDX]--;

        backend.logMessage(player.getNoun() + " upgrades " + TextUtils.format(item.name, 1, false),
                MessageLog.MessageType.GENERAL);
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        Player player = gs.party.player;

        // pay for the upgrade
        if (player.gold < upgradeInfo.price) {
            backend.logMessage(player.getNoun() + " does not have enough money.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.failed();
        }
        player.gold -= upgradeInfo.price;

        DungeonItem gem = player.inventory.items.get(upgradeInfo.gemIdx);

        // upgrade the item
        if (upgradeInfo.equipmentIdx >= 0) {
            DungeonItem item = player.takeOff(upgradeInfo.equipmentIdx);
            upgradeItem(item, gem, backend, player);
            player.wear(item);
            // destroy the gem
            player.inventory.removeOneItemAt(upgradeInfo.gemIdx);
        }
        else if (upgradeInfo.inventoryIdx >= 0) {
            DungeonItem item = player.inventory.items.get(upgradeInfo.inventoryIdx);

            // destroy the gem -- note this is called *after* we get the item to upgrade
            // but before we try to replace items so that we (a) don't mess up
            // upgradeInfo and (b) have space available for a new upgraded item if possible.
            player.inventory.removeOneItemAt(upgradeInfo.gemIdx);

            if (item.count == 1) {
                upgradeItem(item, gem, backend, player);
            } else {
                // clone one item to upgrade
                DungeonItem newItem = new DungeonItem(item);
                newItem.count = 1;
                item.count--;

                upgradeItem(newItem, gem, backend, player);

                // if inventory is full, then drop the stack of non-upgraded items
                if (player.inventory.numCanAdd(newItem) == 0) {
                    player.inventory.items.remove(item);
                    DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
                    square.items.add(item);
                    backend.logMessage(player.getNoun() + " drops " + TextUtils.format(item.name, item.count, false),
                            MessageLog.MessageType.GENERAL);
                }
                player.inventory.add(newItem);
            }
        }

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEventOld.DungeonUpdated());
        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }

    @Override
    public Actor getActor() { return null; }
}
