package pow.backend.action;

import pow.backend.event.GameEvent;

import java.util.*;

public class ActionResult {
    public final List<GameEvent> events;
    public final List<Action> derivedActions;
    public final boolean succeeded;

    private ActionResult(List<GameEvent> events, List<Action> derivedActions, boolean succeeded) {
        this.events = events;
        this.derivedActions = derivedActions;
        this.succeeded = succeeded;
    }

    public static ActionResult succeeded(List<GameEvent> events) {
        return new ActionResult(events, Collections.emptyList(), true);
    }

    public static ActionResult failed(List<Action> derivedActions) {
        return new ActionResult(Collections.emptyList(), derivedActions, false);
    }

    public static ActionResult failed(Action derivedAction) {
        return new ActionResult(Collections.emptyList(), Arrays.asList(derivedAction), false);
    }

    public static ActionResult failed() {
        return new ActionResult(Collections.emptyList(), Collections.emptyList(), false);
    }
}
