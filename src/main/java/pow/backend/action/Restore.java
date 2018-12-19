package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Restore implements Action {
    private final Actor actor;
    private final int amount;

    public Restore(Actor actor, int amount) {
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

        int hpAmount = actor.increaseHealth(this.amount);
        int mpAmount = actor.increaseMana(this.amount);
        backend.logMessage(actor.getNoun() + " restores " + hpAmount + " health and " + mpAmount + " mana",
                MessageLog.MessageType.GENERAL);
        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
