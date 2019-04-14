package pow.backend.ai;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Move;
import pow.backend.actors.Actor;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;

public class StepMovement implements Movement, Serializable {

    @Override
    public Action wander(Actor actor, GameState gs) {
        int dx = gs.rng.nextInt(3) - 1;
        int dy = gs.rng.nextInt(3) - 1;
        return moveOrWait(actor, gs, dx, dy);
    }

    @Override
    public boolean canMoveTowardTarget(Actor actor, GameState gs, Point target) {
        Point dir = getDirectionTowardTarget(actor.loc, target);
        return canMoveTo(actor, gs, actor.loc.x + dir.x, actor.loc.y + dir.y);
    }

    @Override
    public Action moveTowardTarget(Actor actor, GameState gs, Point target) {
        return moveTowardTargetTwoStep(actor, gs, target);
    }

    // Looks ahead 2 steps when determining where to go.  Not as
    // powerful as a full A* search, but is simple and lets
    // monsters follow around corners.
    private Action moveTowardTargetTwoStep(Actor actor, GameState gs, Point target) {
        // find which square 2 steps ahead is closest to the target
        int closestDist = Integer.MAX_VALUE;
        Point twoStepTarget = null;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                // need to be 2 steps away
                if (Math.abs(dx) != 2 && Math.abs(dy) != 2) continue;
                Point twoStep = new Point(actor.loc.x + dx, actor.loc.y + dy);
                if ((twoStep.x != target.x || twoStep.y != target.y) && !canMoveTo(actor, gs, twoStep.x, twoStep.y)) continue;
                int d2 = MathUtils.dist2(twoStep, target);
                if (d2 < closestDist) {
                    closestDist = d2;
                    twoStepTarget = twoStep;
                }
            }
        }

        if (twoStepTarget == null) {
            // No square 2 steps away is closest.
            // Fall back to just heading directly toward the target if possible.
            Point dir = getDirectionTowardTarget(actor.loc, target);
            return moveOrWait(actor, gs, dir.x, dir.y);
        } else {
            // Find the single one-step move that brings us closest to the 2-step target.
            closestDist = Integer.MAX_VALUE;
            Point oneStepDir = new Point(0,0);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Point step = new Point(actor.loc.x + dx, actor.loc.y + dy);
                    if (!canMoveTo(actor, gs, step.x, step.y)) continue;
                    int d2 = MathUtils.dist2(step, twoStepTarget);
                    if (d2 < closestDist) {
                        closestDist = d2;
                        oneStepDir = new Point(dx, dy);
                    }
                }
            }

            return new Move(actor, oneStepDir.x, oneStepDir.y);
        }
    }

    @Override
    public Actor findNearestEnemy(Actor actor, GameState gs) {
        int bestDist = Integer.MAX_VALUE;
        Actor closestMonster = null;
        for (Actor m : gs.getCurrentMap().actors) {
            if (actor.friendly == m.friendly) {
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

    private static boolean canMoveTo(Actor actor, GameState gs, int x, int y) {
        GameMap map = gs.getCurrentMap();
        boolean onMap = map.isOnMap(x, y);
        boolean canSeeTrap = map.hasTrapAt(x, y) && actor.canSeeTraps();
        boolean blocked = map.isBlocked(actor, x, y);
        return (onMap && !blocked && !canSeeTrap);
    }

    private static Action moveOrWait(Actor actor, GameState gs, int dx, int dy) {
        if (canMoveTo(actor, gs, actor.loc.x + dx, actor.loc.y + dy)) {
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
