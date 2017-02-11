package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.event.GameEvent;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class PickUp implements Action {
    private Actor actor;
    private int itemNum;
    private int numToAdd;

    public PickUp(Actor actor, int itemNum, int numToAdd) {
        this.actor = actor;
        this.itemNum = itemNum;
        this.numToAdd = numToAdd;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());

        DungeonSquare square = gs.getCurrentMap().map[actor.loc.x][actor.loc.y];
        if (square.items.size() == 0) {
            backend.logMessage(actor.getPronoun() + " can't pick up anything here.");
            return ActionResult.Failed(null);
        }

        DungeonItem item = square.items.items.get(itemNum);
        // special case for money
        if (item.flags.money) {
            actor.gold += item.count;
            square.items.items.remove(itemNum);
            backend.logMessage(actor.getPronoun() + " pick up " + TextUtils.format(item.name, numToAdd, true));
            return ActionResult.Succeeded(events);
        }

        int numCanGet = Math.min(actor.inventory.numCanAdd(item), item.count);
        if (numCanGet <= 0) {
            backend.logMessage(actor.getPronoun() + " can't hold any more.");
            return ActionResult.Failed(null);
        }

        numToAdd = Math.min(numToAdd, numCanGet); // make sure we don't add more than we are able!
        if (numToAdd == item.count) {
            // if can pick up all, then just transfer the item to inventory
            actor.inventory.add(item);
            square.items.items.remove(itemNum);
        } else {
            // if can just pick up some, then have to clone object, and update counts
            DungeonItem cloneForInventory = new DungeonItem(item);
            cloneForInventory.count = numToAdd;
            item.count -= numToAdd;
            actor.inventory.add(cloneForInventory);
        }
        backend.logMessage(actor.getPronoun() + " pick up " + TextUtils.format(item.name, numToAdd, true));
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
