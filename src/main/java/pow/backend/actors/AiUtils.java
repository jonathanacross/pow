package pow.backend.actors;

import pow.backend.GameState;
import pow.backend.command.CommandRequest;
import pow.backend.command.Move;
import pow.util.MathUtils;

// collection of AI utilities for actors
public class AiUtils {

    public static CommandRequest wander(Actor actor, GameState gs) {
        int dx = gs.rng.nextInt(3) - 1;
        int dy = gs.rng.nextInt(3) - 1;
        return moveOrWait(actor, gs, dx, dy);
    }

    public static CommandRequest moveTowardTarget(Actor actor, GameState gs, int tx, int ty) {
        int d2 = MathUtils.dist2(actor.x, actor.y, tx, ty);

        double dist = Math.sqrt(d2);
        int rdx = tx - actor.x;
        int rdy = ty - actor.y;
        int dx = (int) Math.round(rdx / dist);
        int dy = (int) Math.round(rdy / dist);
        return moveOrWait(actor, gs, dx, dy);
    }

    public static Actor findNearestTarget(Actor actor, GameState gs) {
        int bestDist = Integer.MAX_VALUE;
        Actor closestMonster = null;
        for (Actor m : gs.map.actors) {
            if (actor.friendly != m.friendly) {
                int d2 = MathUtils.dist2(actor.x, actor.y, m.x, m.y);
                if (closestMonster == null || d2 < bestDist) {
                    closestMonster = m;
                    bestDist = d2;
                }
            }
        }
        return closestMonster;
    }

    private static CommandRequest moveOrWait(Actor actor, GameState gs, int dx, int dy) {
        if (!gs.map.isBlocked(actor.x + dx, actor.y + dy)) {
            return new Move(actor, dx, dy);
        } else {
            return new Move(actor, 0, 0);
        }
    }

}
