package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class EnterPortal implements Action {

    private final Actor actor;

    public EnterPortal(Actor actor) {
        this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.IN_PORTAL);  // trigger the frontend to pop open a window to see what to do.

        return ActionResult.succeeded(events);
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
