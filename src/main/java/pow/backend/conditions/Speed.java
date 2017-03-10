package pow.backend.conditions;

import pow.backend.actors.Actor;

public class Speed extends Condition {
    public Speed(Actor actor) {
        super(actor);
    }

    @Override
    String getStartMessage() { return actor.getPronoun() + " start moving faster!"; }

    @Override
    String getEndMessage() { return actor.getPronoun() + " return to normal speed."; }
}
