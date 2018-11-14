package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.actors.Actor;
import pow.backend.actors.ai.PetAi;

import java.io.Serializable;

public class AiBehavior implements Behavior, Serializable {

    private final Actor actor;
    private final GameState gs;

    public AiBehavior(Actor actor, GameState gs) {
        this.actor = actor;
        this.gs = gs;
    }

    @Override
    public boolean canPerform(GameState gameState) {
        return true;
    }

    @Override
    public Action getAction() {
        return PetAi.getAction(actor, gs);
    }
}
