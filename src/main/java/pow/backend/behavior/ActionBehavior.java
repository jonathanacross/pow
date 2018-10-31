package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;

public class ActionBehavior implements Behavior {

    private final Actor actor;
    private final Action action;

    public ActionBehavior(Actor actor, Action action) {
        this.actor = actor;
        this.action = action;
    }

    @Override
    public boolean canPerform(GameState gameState) {
        return true;
    }

    @Override
    public pow.backend.action.Action getAction() {
        actor.clearBehavior();
        return action;
    }
}
