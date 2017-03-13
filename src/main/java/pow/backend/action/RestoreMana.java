package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class RestoreMana implements Action {
    private Actor actor;
    private int amount;

    public RestoreMana(Actor actor, int amount) {
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

        int restoreAmount = Math.min(this.amount, actor.getMaxMana() - actor.getMana());
        actor.mana += restoreAmount;
        backend.logMessage(actor.getPronoun() + " restored " + restoreAmount + " mana");
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
