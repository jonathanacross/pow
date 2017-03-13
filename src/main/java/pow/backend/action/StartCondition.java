package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

// TODO: make this to StartConditions, make constructor take list, for heroism
public class StartCondition implements Action {

    // TODO: see if there's a way to reduce the code duplication of listing different conditions
    // (also in Actor.java).
    public enum ConditionType {
        HEALTH,
        POISON,
        SPEED,
        TO_HIT,
        TO_DAM,
        DEFENSE
    }

    private final Actor actor;
    private final ConditionType conditionType;
    private final int turnCount;
    private final int bonus;

    public StartCondition(Actor actor, ConditionType conditionType, int turnCount, int bonus) {
        this.actor = actor;
        this.conditionType = conditionType;
        this.turnCount = turnCount;
        this.bonus = bonus;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        switch (conditionType) {
            case HEALTH: events.addAll(actor.conditions.health.start(backend, turnCount, bonus)); break;
            case POISON: events.addAll(actor.conditions.poison.start(backend, turnCount, bonus)); break;
            case SPEED: events.addAll(actor.conditions.speed.start(backend, turnCount, bonus)); break;
            case TO_HIT: events.addAll(actor.conditions.toHit.start(backend, turnCount, bonus)); break;
            case TO_DAM: events.addAll(actor.conditions.toDam.start(backend, turnCount, bonus)); break;
            case DEFENSE: events.addAll(actor.conditions.defense.start(backend, turnCount, bonus)); break;
        }
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
