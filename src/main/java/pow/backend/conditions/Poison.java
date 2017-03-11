package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.List;

public class Poison extends Condition implements Serializable {
    public Poison(Actor actor) {
        super(actor);
    }

    @Override
    String getStartMessage(){
        return(actor.getPronoun() + " are poisoned!");
    }

    @Override
    String getEndMessage() {
        return(actor.getPronoun() + " recover from the poison.");
    }

    @Override
    protected List<GameEvent> updateImpl(GameBackend backend) {
        return actor.takeDamage(backend, getIntensity());
    }
}
