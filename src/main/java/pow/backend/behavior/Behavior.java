package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.Action;

public interface Behavior {
    boolean canPerform(GameState gameState);
    Action getAction();
}
