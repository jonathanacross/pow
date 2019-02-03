package pow.backend.action;

import pow.backend.ActionParams;
import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;
import pow.util.DebugLogger;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;


public class FeedPet implements Action {
    private final Actor player;
    private final Actor pet;
    private final ItemList itemList;
    private final int itemIdx;

    public FeedPet(Actor player, Actor pet, ItemList itemList, int itemIdx) {
        this.player = player;
        this.pet = pet;
        this.itemList = itemList;
        this.itemIdx = itemIdx;
    }

    @Override
    public Actor getActor() {
        return this.player;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        DungeonItem item = itemList.items.get(itemIdx);
        if (!item.flags.potion) {
            DebugLogger.fatal(new RuntimeException(player.getNoun() + " tried to feed a non-potion, " + item.name));
            return ActionResult.failed();
        }

        backend.logMessage(player.getNoun() + " feeds " + TextUtils.format(item.name, 1, false)
                        + " to " + pet.getNoun(),
                MessageLog.MessageType.GENERAL);
        itemList.removeOneItemAt(itemIdx);

        // get the real action from the potion and do it
        Action action = ActionParams.buildAction(this.pet, item.actionParams);
        List<Action> subactions = new ArrayList<>();
        subactions.add(action);
        subactions.add(new CompletedAction(player));
        return ActionResult.failed(subactions);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
