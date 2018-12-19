package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

// A meta action used at the end of a list of derived actions
// to mark the original action as complete and use energy.
public class CompletedAction implements Action {

    private Actor actor;

    public CompletedAction(Actor actor) {
        this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return ActionResult.succeeded();
    }

    @Override
    public boolean consumesEnergy() {
        return true;
    }

    @Override
    public Actor getActor() {
        return actor;
    }
}
