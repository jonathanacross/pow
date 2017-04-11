package pow.backend.actors.ai;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Move;
import pow.backend.actors.Actor;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.*;

public class KnightMovement implements Movement, Serializable {

    private static final List<Point> knightMoves = Arrays.asList(
                new Point(2, 1),
                new Point(1, 2),
                new Point(-1, 2),
                new Point(-2, 1),
                new Point(-2, -1),
                new Point(-1, -2),
                new Point(1, -2),
                new Point(2, -1));

    // Score of how bad the distance squared is from the knight to its
    // target, smaller is better. If it is dist 1 or 2, then the target
    // can hit the knight, but knight can't hit the target, so it has a
    // big score.  Dist^2 = 5 is optimal, since the knight can attack.
    private static final Map<Integer, Integer> dist2ToScore;
    static {
        dist2ToScore = new HashMap<>();
        dist2ToScore.put(1, 10);
        dist2ToScore.put(2, 10);
        dist2ToScore.put(4, 1);
        dist2ToScore.put(5, 0);
        dist2ToScore.put(8, 3);
        dist2ToScore.put(9, 2);
        dist2ToScore.put(10, 1);
        dist2ToScore.put(13, 2);
        dist2ToScore.put(16, 1);
        dist2ToScore.put(17, 2);
        dist2ToScore.put(20, 1);
        dist2ToScore.put(25, 2);
        dist2ToScore.put(32, 3);
    }

    @Override
    public Action wander(Actor actor, GameState gs) {
        Point wanderMove = new Point(0,0);
        int moveCount = 1;
        for (Point move : knightMoves) {
            int newx = actor.loc.x + move.x;
            int newy = actor.loc.y + move.y;

            // skip illegal moves
            if (!gs.getCurrentMap().isOnMap(newx, newy) ||
                    gs.getCurrentMap().isBlocked(actor, newx, newy)) {
                continue;
            }

            // Uses reservoir sampling (with a reservoir of size 1!) to pick a legal
            // move with equal probability.
            if (gs.rng.nextInt(moveCount) == 0) {
                wanderMove = move;
            }
            moveCount++;
        }

        return new Move(actor, wanderMove.x, wanderMove.y);
    }

    @Override
    public Action moveTowardTarget(Actor actor, GameState gs, Point target) {
        Point bestMove = getClosestMoveTowardTarget(actor, gs, target);
        return new Move(actor, bestMove.x, bestMove.y);
    }

    @Override
    public boolean canMoveTowardTarget(Actor actor, GameState gs, Point target) {
        Point bestMove = getClosestMoveTowardTarget(actor, gs, target);
        return bestMove.x != 0 || bestMove.y != 0;
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

            int dist2 = MathUtils.dist2(actor.loc.x, actor.loc.y, m.loc.x, m.loc.y);
            int currScore = dist2ToScore.containsKey(dist2) ?
                    dist2ToScore.get(dist2) : dist2;

            if (closestMonster == null || currScore < bestDist) {
                closestMonster = m;
                bestDist = currScore;
            }
        }
        return closestMonster;
    }

    @Override
    public boolean canHit(Actor actor, Actor target) {
        int dist2 = MathUtils.dist2(actor.loc, target.loc);
        return dist2 == 5;
    }

    private static Point getClosestMoveTowardTarget(Actor actor, GameState gs, Point target) {

        Point bestMove = new Point(0,0);  // If nothing better found, stay put.
        int bestScore = Integer.MAX_VALUE;
        for (Point move : knightMoves) {
            int newx = actor.loc.x + move.x;
            int newy = actor.loc.y + move.y;

            // skip illegal moves
            if (!gs.getCurrentMap().isOnMap(newx, newy) ||
                    gs.getCurrentMap().isBlocked(actor, newx, newy)) {
                continue;
            }

            int trialDist2 = MathUtils.dist2(newx, newy, target.x, target.y);
            int currScore = dist2ToScore.containsKey(trialDist2) ?
                    dist2ToScore.get(trialDist2) : trialDist2;
            if (currScore < bestScore) {
                bestMove = move;
                bestScore = currScore;
            }
        }

        return bestMove;
    }
}
