package pow.backend.actors;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Move;
import pow.util.MathUtils;
import pow.util.Point;

// collection of AI utilities for actors
public class AiUtils {

    public static Action wander(Actor actor, GameState gs) {
        int dx = gs.rng.nextInt(3) - 1;
        int dy = gs.rng.nextInt(3) - 1;
        return moveOrWait(actor, gs, dx, dy);
    }

    public static Action moveTowardTarget(Actor actor, GameState gs, Point target) {
        int d2 = MathUtils.dist2(actor.loc, target);

        double dist = Math.sqrt(d2);
        int rdx = target.x - actor.loc.x;
        int rdy = target.y - actor.loc.y;
        int dx = (int) Math.round(rdx / dist);
        int dy = (int) Math.round(rdy / dist);
        return moveOrWait(actor, gs, dx, dy);
    }

    public static Actor findNearestTarget(Actor actor, GameState gs) {
        int bestDist = Integer.MAX_VALUE;
        Actor closestMonster = null;
        for (Actor m : gs.world.currentMap.actors) {
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

    private static Action moveOrWait(Actor actor, GameState gs, int dx, int dy) {
        if (!gs.world.currentMap.isBlocked(actor.loc.x + dx, actor.loc.y + dy)) {
            return new Move(actor, dx, dy);
        } else {
            return new Move(actor, 0, 0);
        }
    }

}
