package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.conditions.ConditionTypes;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class StartCondition implements Action {

    private final Actor actor;
    private final List<ConditionTypes> conditionTypes;
    private final int turnCount;
    private final int bonus;

    public StartCondition(Actor actor, List<ConditionTypes> conditionTypes, int turnCount, int bonus) {
        this.actor = actor;
        this.conditionTypes = conditionTypes;
        this.turnCount = turnCount;
        this.bonus = bonus;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        for (ConditionTypes conditionType : conditionTypes) {
            events.addAll(actor.conditions.get(conditionType).start(backend, turnCount, bonus));
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
