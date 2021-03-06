package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

// Note this is different from heal, which can heal a % of health;
// this just restores an absoulte amount.
public class RestoreHealth implements Action {
    private final Actor actor;
    private final int amount;

    public RestoreHealth(Actor actor, int amount) {
        this.actor = actor;
        this.amount = amount;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.HEALED);

        int restoreAmount = actor.increaseHealth(this.amount);
        backend.logMessage(actor.getNoun() + " heals " + restoreAmount, MessageLog.MessageType.GENERAL);
        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
