package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

import java.util.ArrayList;

public class Rest implements CommandRequest {

    private Actor actor;

    public Rest(Actor actor) {
        this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return ActionResult.Succeeded(new ArrayList<>());
    }

    @Override
    public boolean consumesEnergy() {
        return true;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }
}
