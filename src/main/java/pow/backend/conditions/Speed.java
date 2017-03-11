package pow.backend.conditions;

import pow.backend.actors.Actor;

import java.io.Serializable;

public class Speed extends Condition implements Serializable {
    public Speed(Actor actor) {
        super(actor);
    }

    @Override
    String getStartMessage() { return actor.getPronoun() + " start moving faster!"; }

    @Override
    String getEndMessage() { return actor.getPronoun() + " return to normal speed."; }
}
