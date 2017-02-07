package pow.frontend.utils.targeting;

import pow.util.Point;
import pow.util.direction.Direction;

import java.util.List;

public class TargetingUtils {
    // sort the points by distance to a given square, so that we can do things like
    //  (1) target the monster closest to the player by default
    //  (2) cycle through targets with the space key in a sensible order
    public static void orderPointsByDistance(Point current, List<Point> targetablePoints) {
        targetablePoints.sort(
                (Point a, Point b) -> {
                    int distA = (a.x - current.x) * (a.x - current.x) + (a.y - current.y) * (a.y - current.y);
                    int distB = (b.x - current.x) * (b.x - current.x) + (b.y - current.y) * (b.y - current.y);
                    return Integer.compare(distA, distB);
                });
    }

    public static int pickTarget(int currIdx, Direction dir, List<Point> targets) {
        int bestIdx = -1, bestDist = Integer.MAX_VALUE;
        Point current = targets.get(currIdx);

        for (int i = 0; i < targets.size(); i++) {
            // compute directed, absolute distance from the target to the current point
            int dx = (targets.get(i).x - current.x);
            int dy = (targets.get(i).y - current.y);
            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);

            // skip if the target isn't in the right direction
            if ((dir.dx != 0) && (dx * dir.dx <= 0)) continue;
            if ((dir.dy != 0) && (dy * dir.dy <= 0)) continue;
            if ((dir.dy != 0) && (dir.dx == 0) && (absDx > absDy)) continue;
            if ((dir.dx != 0) && (dir.dy == 0) && (absDy > absDx)) continue;

            // approximate distance*2 to find the closest point
            int dist = ((absDx > absDy) ? (absDx + absDx + absDy) : (absDy + absDy + absDx));

            if ((bestIdx < 0) || (dist < bestDist)) {
                bestIdx = i;
                bestDist = dist;
            }
        }

        if (bestIdx < 0)
            return currIdx;
        else
            return bestIdx;
    }
}

