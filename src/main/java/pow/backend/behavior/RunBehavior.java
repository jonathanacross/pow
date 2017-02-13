package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Move;
import pow.backend.actors.Player;
import pow.util.Point;
import pow.util.direction.Direction;

public class RunBehavior implements Behavior {

    Player player;
    Direction direction;

    public RunBehavior(Player player, int dx, int dy) {
        this.player = player;
        this.direction = new Direction(dx, dy);
    }

    @Override
    public boolean canPerform(GameState gameState) {
        Point nextStep = new Point(gameState.player.loc.x, gameState.player.loc.y);
        nextStep.x += direction.dx;
        nextStep.y += direction.dy;
        return (gameState.getCurrentMap().isOnMap(nextStep.x, nextStep.y) &&
                !gameState.getCurrentMap().isBlocked(nextStep.x, nextStep.y));
    }

    @Override
    public Action getAction() {
        return new Move(player, direction.dx, direction.dy);
    }
}
