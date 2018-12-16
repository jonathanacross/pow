package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;

import java.util.ArrayList;
import java.util.List;

public class Heal implements Action {
    private final Actor actor;
    private final int amount;

    public Heal(Actor actor, int amount) {
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
        events.add(GameEventOld.Healed());

        // x% or x hp, whichever is greater
        int desiredHealAmount = Math.max((int)Math.round(actor.getMaxHealth() * 0.01 * this.amount), this.amount);
        int healAmount = actor.increaseHealth(desiredHealAmount);
        backend.logMessage(actor.getNoun() + " heals " + healAmount, MessageLog.MessageType.GENERAL);
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
