package pow.backend.action.spell;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.backend.action.Action;
import pow.backend.action.ActionResult;
import pow.backend.actors.Actor;

public class SpellAction implements Action {

    private Action action;
    private SpellParams params;

    public SpellAction(Action action, SpellParams params) {
        this.action = action;
        this.params = params;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        Actor actor = action.getActor();
        if (actor.getMana() < params.requiredMana) {
            backend.logMessage(actor.getPronoun() + " doesn't have enough mana.");
            return ActionResult.Failed(null);
        }

        actor.useMana(params.requiredMana);
        return action.process(backend);
    }

    @Override
    public boolean consumesEnergy() {
        return action.consumesEnergy();
    }

    @Override
    public Actor getActor() {
        return action.getActor();
    }
}
