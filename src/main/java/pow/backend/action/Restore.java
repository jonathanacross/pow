package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Restore implements Action {
    private Actor actor;
    private int amount;

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
        events.add(GameEvent.Healed());

        int hpAmount = Math.min(this.amount, actor.getMaxHealth() - actor.health);
        actor.health += hpAmount;
        int mpAmount = Math.min(this.amount, actor.maxMana - actor.mana);
        actor.mana += mpAmount;
        backend.logMessage(actor.getPronoun() + " restored " + hpAmount + " health and " + mpAmount + " mana");
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
