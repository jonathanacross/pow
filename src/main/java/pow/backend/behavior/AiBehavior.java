package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Attack;
import pow.backend.actors.Actor;
import pow.util.MathUtils;

import java.io.Serializable;

import static pow.util.MathUtils.dist2;

public class AiBehavior implements Behavior, Serializable {

    private Actor actor;
    private GameState gs;

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

        if (actor.isConfused()) {
            return actor.movement.wander(actor, gs);
        }

        // try to attack first
        Actor closestEnemy = actor.movement.findNearestEnemy(actor, gs);
        if (closestEnemy != null && MathUtils.dist2(actor.loc, closestEnemy.loc) <= 2) {
            return new Attack(actor, closestEnemy);
        }

        // if "far away" from the human controlled player, then try to catch up
        int playerDist = dist2(actor.loc, gs.selectedActor.loc);
        if (playerDist >= 9) {
            return actor.movement.moveTowardTarget(actor, gs, gs.player.loc);
        }

        // move randomly
        return actor.movement.wander(actor, gs);
    }
}
