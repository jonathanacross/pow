package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.actors.Player;

public class ActionBehavior implements Behavior {

    private final Player player;
    private final Action action;

    public ActionBehavior(Player player, Action action) {
        this.player = player;
        this.action = action;
    }

    @Override
    public boolean canPerform(GameState gameState) {
        return true;
    }

    @Override
    public pow.backend.action.Action getAction() {
        player.waitForInput();
        return action;
    }
}
