package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class Swap implements Action {
    private final Actor first;
    private final Actor second;

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
            backend.logMessage(first.getNoun() + " and " + second.getNoun() + " swap places",
                    MessageLog.MessageType.GENERAL);
            events.add(GameEvent.MOVED);
            return ActionResult.succeeded(events);
        } else {
            // tried to swap self.
            return ActionResult.failed();
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
