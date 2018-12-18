package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;
import pow.util.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class TransferItem implements Action {
    private final Actor giver;
    private final Actor taker;
    private final int itemNum;
    private final int numToTransfer;
    public TransferItem(Actor giver, Actor taker, int itemNum, int numToTransfer) {
        this.giver = giver;
        this.taker = taker;
        this.itemNum = itemNum;
        this.numToTransfer = numToTransfer;
    }
    @Override
    public Actor getActor() {
        return this.giver;
    }
    @Override
    public ActionResult process(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEventOld.DungeonUpdated());
        DungeonItem item = giver.inventory.items.get(itemNum);
        int numCanTransfer = Math.min(numToTransfer, taker.inventory.numCanAdd(item));
        if (numCanTransfer == 0) {
            backend.logMessage(taker.getNoun() + " can't hold any more.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.failed();
        }
        if (numCanTransfer == item.count) {
            // giving all of this item
            giver.inventory.items.remove(itemNum);
            taker.inventory.add(item);
        } else {
            // if can just transfer some, then have to clone object, and update counts
            DungeonItem cloneForTaker = new DungeonItem(item);
            cloneForTaker.count = numCanTransfer;
            item.count -= numCanTransfer;
            taker.inventory.add(cloneForTaker);
        }
        backend.logMessage(giver.getNoun() + " gives " +
                TextUtils.format(item.name, numCanTransfer, true) + " to " + taker.getNoun(),
                MessageLog.MessageType.GENERAL);
        return ActionResult.succeeded(events);
    }
    @Override
    public boolean consumesEnergy() { return true; }
}
