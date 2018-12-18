package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;

import java.util.ArrayList;
import java.util.List;

public class EnterPortal implements Action {

    private final Actor actor;

    public EnterPortal(Actor actor) {
        this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        backend.getGameState().inPortal = true;

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEventOld.InPortal());  // trigger the frontend to pop open a window to see what to do.

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
