package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

public class Poison extends Condition {
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
    protected void updateImpl(GameBackend backend) {
        actor.takeDamage(backend, getIntensity());
    }
}
