package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

public class UpdateConditions implements Action {

    private final Actor actor;

    public UpdateConditions(Actor actor) {
        this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return ActionResult.succeeded(actor.conditions.update(backend));
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public Actor getActor() {
        return actor;
    }
}
