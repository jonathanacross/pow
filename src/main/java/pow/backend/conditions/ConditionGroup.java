package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class to store an instance of all the conditions.
// One instance will be created per actor.
public class ConditionGroup implements Serializable {
    public Map<ConditionTypes, Condition> conditionMap;

    public ConditionGroup(Actor actor) {
        conditionMap = new HashMap<>();
        for (ConditionTypes type : ConditionTypes.values()) {
            conditionMap.put(type, type.getInstance(actor));
        }
    }

    public List<GameEvent> update(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        for (Condition condition : conditionMap.values()) {
            events.addAll(condition.update(backend));
        }
        return events;
    }

    public Condition get(ConditionTypes type) {
        return conditionMap.get(type);
    }
}

