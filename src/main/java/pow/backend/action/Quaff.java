package pow.backend.action;

import pow.backend.ActionParams;
import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.util.DebugLogger;
import pow.util.TextUtils;

public class Quaff implements Action {
    private Actor actor;
    private int itemIdx;

    public Quaff(Actor actor, int itemIdx) {
        this.actor = actor;
        this.itemIdx = itemIdx;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        DungeonItem item = actor.inventory.items.get(itemIdx);
        if (!item.flags.potion) {
            DebugLogger.fatal(new RuntimeException(actor.name + " tried to quaff a non-potion, " + item.name));
            return ActionResult.Failed(null);
        }

        // get the real action from the potion and do it
        backend.logMessage(actor.getPronoun() + " quaffed " + TextUtils.format(item.name, 1, false));
        actor.inventory.removeOneItemAt(itemIdx);
        Action action = ActionParams.buildAction(this.actor, item.actionParams);
        return action.process(backend);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
