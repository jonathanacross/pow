package pow.backend.ai;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.util.Bresenham;
import pow.util.MathUtils;
import pow.util.Point;

import java.util.List;

public class AiUtils {
    public static boolean enemyIsWithinRange(Actor me, Actor target, int radius) {
        if (target == null) {
            return false;
        }
        int dist2 = MathUtils.dist2(me.loc, target.loc);
        return dist2 < radius * radius;
    }

    public static boolean actorHasLineOfSight(Actor actor, GameState gs, Point target) {
        GameMap map = gs.getCurrentMap();
        int radius = Math.abs(target.x - actor.loc.x) + Math.abs(target.y - actor.loc.y);
        List<Point> ray = Bresenham.makeRay(actor.loc, target, radius + 1);
        ray.remove(0); // remove the actor from the path
        for (Point p : ray) {
            if (!map.isOnMap(p.x, p.y)) return false;
            if (map.map[p.x][p.y].blockAir()) return false;
            if (p.x == target.x && p.y == target.y) return true;
            // if not the target, avoid if going to hit another monster
            if (map.actorAt(p.x, p.y) != null) return false;
        }
        return false;
    }
}
