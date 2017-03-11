package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Health extends Condition implements Serializable {

    public Health(Actor actor) {
        super(actor);
    }

    @Override
    String getStartMessage() {
        return actor.getPronoun() + " feel vigorous.";
    }

    @Override
    String getEndMessage() {
        return actor.getPronoun() + " return to normal.";
    }

    @Override
    // TODO: this will break if the player adds/removes equipment!
    // Perhaps move 'updateStats' to be a base method in actor
    // which would include the lines below.
    protected List<GameEvent> startImpl(GameBackend backend) {
        actor.maxHealth += getIntensity();
        actor.health += getIntensity();
        return Arrays.asList(GameEvent.DungeonUpdated());
    }

    @Override
    protected List<GameEvent> endImpl(GameBackend backend) {
        actor.maxHealth -= getIntensity();
        actor.health = Math.min(actor.health, actor.maxHealth);
        return Arrays.asList(GameEvent.DungeonUpdated());
    }
}
