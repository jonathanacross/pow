package pow.backend.ai;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Move;
import pow.backend.actors.Actor;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;

public class StationaryMovement implements Movement, Serializable {

    @Override
    public Action wander(Actor actor, GameState gs) {
        return new Move(actor, 0, 0);
    }

    @Override
    public Action moveTowardTarget(Actor actor, GameState gs, Point target) {
        return new Move(actor, 0, 0);
    }

    @Override
    public boolean canMoveTowardTarget(Actor actor, GameState gs, Point target) {
        return false;
    }

    @Override
    public Actor findNearestEnemy(Actor actor, GameState gs) {
        return findNearestActor(actor, gs, true);
    }

    @Override
    public Actor findNearestActor(Actor actor, GameState gs) {
        return findNearestActor(actor, gs, false);
    }

    private Actor findNearestActor(Actor actor, GameState gs, boolean enemyOnly) {
        int bestDist = Integer.MAX_VALUE;
        Actor closestMonster = null;
        for (Actor m : gs.getCurrentMap().actors) {
            if (enemyOnly && actor.friendly == m.friendly) {
                continue;
            }
            int d2 = MathUtils.dist2(actor.loc, m.loc);
            if (closestMonster == null || d2 < bestDist) {
                closestMonster = m;
                bestDist = d2;
            }
        }
        return closestMonster;
    }

    @Override
    public boolean canHit(Actor actor, Actor target) {
        return MathUtils.dist2(actor.loc, target.loc) <= 2;
    }
}
