package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

// TODO: see if this is game behavior we want.  It might be
// nice to remove some energy from both parties, otherwise it
// looks a little odd if you swap with a pet and then it moves;
// it essentially got a free move!
public class Swap implements Action {
    private Actor first;
    private Actor second;

    public Swap(Actor first, Actor second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Actor getActor() {
        return first;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();

        if (first != second) {
            Point tmp = first.loc; first.loc = second.loc; second.loc = tmp;
            backend.logMessage(first.getPronoun() + " and " + second.getPronoun() + " swap places");
            events.add(GameEvent.Moved());
            return ActionResult.Succeeded(events);
        } else {
            // tried to swap self.
            return ActionResult.Failed(null);
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
