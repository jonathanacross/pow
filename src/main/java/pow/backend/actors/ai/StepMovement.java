package pow.backend.actors.ai;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Move;
import pow.backend.actors.Actor;
import pow.util.MathUtils;
import pow.util.Point;

public class StepMovement implements Movement {

    @Override
    public Action wander(Actor actor, GameState gs) {
        int dx = gs.rng.nextInt(3) - 1;
        int dy = gs.rng.nextInt(3) - 1;
        return moveOrWait(actor, gs, dx, dy);
    }

    @Override
    public boolean canMoveTowardTarget(Actor actor, GameState gs, Point target) {
        Point dir = getDirectionTowardTarget(actor.loc, target);
        return (!gs.getCurrentMap().isBlocked(actor, actor.loc.x + dir.x, actor.loc.y + dir.y));
    }

    @Override
    public Action moveTowardTarget(Actor actor, GameState gs, Point target) {
        Point dir = getDirectionTowardTarget(actor.loc, target);
        return moveOrWait(actor, gs, dir.x, dir.y);
    }

    @Override
    public Actor findNearestTarget(Actor actor, GameState gs) {
        int bestDist = Integer.MAX_VALUE;
        Actor closestMonster = null;
        for (Actor m : gs.getCurrentMap().actors) {
            if (actor.friendly != m.friendly) {
                int d2 = MathUtils.dist2(actor.loc, m.loc);
                if (closestMonster == null || d2 < bestDist) {
                    closestMonster = m;
                    bestDist = d2;
                }
            }
        }
        return closestMonster;
    }

    @Override
    public boolean canHit(Actor actor, Actor target) {
        int dist2 = MathUtils.dist2(actor.loc, target.loc);
        return dist2 <= 2;
    }

    private static Action moveOrWait(Actor actor, GameState gs, int dx, int dy) {
        if (!gs.getCurrentMap().isBlocked(actor, actor.loc.x + dx, actor.loc.y + dy)) {
            return new Move(actor, dx, dy);
        } else {
            return new Move(actor, 0, 0);
        }
    }

    private static Point getDirectionTowardTarget(Point location, Point target) {
        int d2 = MathUtils.dist2(location, target);

        double dist = Math.sqrt(d2);
        int rdx = target.x - location.x;
        int rdy = target.y - location.y;
        int dx = (int) Math.round(rdx / dist);
        int dy = (int) Math.round(rdy / dist);
        return new Point(dx, dy);
    }

}
