package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.event.GameEvent;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Drop implements Action {
    private final Actor actor;
    private final int itemNum;
    private final int numToDrop;

    public Drop(Actor actor, int itemNum, int numToDrop) {
        this.actor = actor;
        this.itemNum = itemNum;
        this.numToDrop = numToDrop;
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
        DungeonItem item = actor.inventory.items.get(itemNum);
        if (numToDrop == item.count) {
            // dropping all of this item
            actor.inventory.items.remove(itemNum);
            square.items.add(item);
        } else {
            // if can just pick up some, then have to clone object, and update counts
            DungeonItem cloneForFloor = new DungeonItem(item);
            cloneForFloor.count = numToDrop;
            item.count -= numToDrop;
            square.items.add(cloneForFloor);
        }
        backend.logMessage(actor.getNoun() + " drops " + TextUtils.format(item.name, numToDrop, true),
                MessageLog.MessageType.GENERAL);
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
