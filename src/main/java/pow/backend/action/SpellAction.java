package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.SpellParams;
import pow.backend.actors.Actor;

import java.util.ArrayList;
import java.util.List;

public class SpellAction implements Action {

    private final Action action;
    private final SpellParams params;

    public SpellAction(Action action, SpellParams params) {
        this.action = action;
        this.params = params;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        Actor actor = action.getActor();
        if (actor.getMana() < params.requiredMana) {
            backend.logMessage(actor.getNoun() + " doesn't have enough mana.",
                    MessageLog.MessageType.USER_ERROR);
            return ActionResult.failed();
        }

        // Use the mana, then requeue the actual spell action.
        actor.useMana(params.requiredMana);
        backend.logMessage(params.getCastMessage(actor), MessageLog.MessageType.COMBAT_NEUTRAL);

        List<Action> subactions = new ArrayList<>();
        subactions.add(action);
        subactions.add(new CompletedAction(actor));
        return ActionResult.failed(subactions);
    }

    @Override
    public boolean consumesEnergy() { return false; }

    @Override
    public Actor getActor() {
        return action.getActor();
    }
}
