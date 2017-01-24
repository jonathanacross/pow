package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Heal implements Action {
    private Actor actor;
    private int amount;

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
        events.add(GameEvent.Healed());

        int healAmount = Math.min(this.amount, actor.maxHealth - actor.health);
        actor.health += healAmount;
        backend.logMessage(actor.getPronoun() + " healed " + healAmount);
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
