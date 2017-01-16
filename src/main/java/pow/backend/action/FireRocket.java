package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.Arrays;

public class FireRocket implements Action {

    private Actor actor;

    public FireRocket(Actor actor) {
       this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        backend.logMessage(actor.getPronoun() + " fire a rocket");
        return ActionResult.Succeeded(Arrays.asList(GameEvent.Rocket(actor)));
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public boolean consumesEnergy() { return true; }
}