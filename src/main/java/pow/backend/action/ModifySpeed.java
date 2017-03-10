package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class ModifySpeed implements Action {
    private Actor actor;
    int turnCount;
    int bonus;

    public ModifySpeed(Actor actor, int turnCount, int bonus) {
        this.actor = actor;
        this.turnCount = turnCount;
        this.bonus = bonus;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        actor.conditions.speed.start(backend, turnCount, bonus);
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }
}
