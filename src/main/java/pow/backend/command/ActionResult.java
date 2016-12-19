package pow.backend.command;

import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class ActionResult {
    // TODO: make getters
    public List<GameEvent> events;
    public boolean succeeded;
    public boolean done;
    public CommandRequest alternate;

    public ActionResult(List<GameEvent> events, boolean succeeded, boolean done, CommandRequest alternate) {
        this.events = events;
        this.succeeded = succeeded;
        this.done = done;
        this.alternate = alternate;
    }

    public static ActionResult Succeeded(List<GameEvent> events) {
        return new ActionResult(events, true, true, null);
    }

    public static ActionResult Failed(CommandRequest alternate) {
        return new ActionResult(new ArrayList<>(), false, true, alternate);
    }

    public static ActionResult NotDone(List<GameEvent> events) {
        return new ActionResult(events, true, false, null);
    }
}
