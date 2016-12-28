package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Attack;
import pow.backend.action.Action;
import pow.util.MathUtils;

import java.io.Serializable;

import static pow.util.MathUtils.dist2;

public class Pet extends Actor implements Serializable {

    public Pet(String id, String name, String image, String description, int x, int y) {
        super(id, name, image, description, x, y, true, 5, true, 0);
    }

    @Override
    public String getPronoun() {
        return this.name;
    }

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public Action act(GameBackend backend) {
        GameState gs = backend.getGameState();

        // try to attack first
        Actor closestEnemy = AiUtils.findNearestTarget(this, gs);
        if (closestEnemy != null && MathUtils.dist2(loc, closestEnemy.loc) <= 2) {
            return new Attack(this, closestEnemy);
        }

        // if "far away" from the player, then try to catch up
        int playerDist = dist2(loc, gs.player.loc);
        if (playerDist >= 9) {
            return AiUtils.moveTowardTarget(this, gs, gs.player.loc);
        }

        // move randomly
        return AiUtils.wander(this, gs);
    }
}
