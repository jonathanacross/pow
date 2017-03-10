package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

public class Health extends Condition {

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
    protected void startImpl(GameBackend backend) {
        actor.maxHealth += getIntensity();
        actor.health += getIntensity();
    }

    @Override
    protected void endImpl(GameBackend backend) {
        actor.maxHealth -= getIntensity();
        actor.health = Math.min(actor.health, actor.maxHealth);
    }
}
