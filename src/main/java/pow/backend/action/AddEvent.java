package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

// A meta action used to add an event in the queue.
// Why not just add the event directly?  Because sometimes we want
// the event to be delayed after other actions.  E.g., to update
// player status *after* their primary action has completed
public class AddEvent implements Action {

    GameEvent event;

    public AddEvent(GameEvent event) {
        this.event = event;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return ActionResult.succeeded(event);
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public Actor getActor() {
        return null;
    }
}
