package pow.backend.action;

import pow.backend.*;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Metric;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class BallSpell implements Action {

    private final Actor actor;
    private final Point target;
    private final SpellParams spellParams;

    public BallSpell(Actor actor, Point target, SpellParams spellParams) {
        this.actor = actor;
        this.target = target;
        this.spellParams = spellParams;
    }

    // Player may have targeted some illegal square, or some square
    // they can't see.  So force the target to be the closest square
    // towards the desired target that they *can* see.
    private Point getVisibleTarget(GameMap map, Point target) {
        int radius = Math.abs(target.x - actor.loc.x) + Math.abs(target.y - actor.loc.y);

        List<Point> ray = Bresenham.makeRay(actor.loc, target, radius + 1);
        Point visibleTarget = actor.loc;
        for (Point p : ray) {
            if (!map.isOnMap(p.x, p.y)) break;
            if (map.map[p.x][p.y].blockAir()) break;
            visibleTarget = p;
            if (p.x == target.x && p.y == target.y) break;
        }
        return visibleTarget;
    }

    private static List<Point> getBallArea(GameState gameState, Point center, int radius) {
        return SpellUtils.getFieldOfView(gameState, center, radius, Metric.euclideanMetric);
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        Point visibleTarget = getVisibleTarget(gs.getCurrentMap(), target);

        // draw effects
        String effectName = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.LARGE_BALL,
                SpellUtils.getEffectColor(spellParams.element),
                Direction.N); // dummy
        for (int radius = 1; radius <= spellParams.size; radius++) {
            List<Point> effectSquares = getBallArea(gs, visibleTarget, radius);
            events.add(GameEvent.Effect(new DungeonEffect(effectName, effectSquares)));
        }
        for (int radius = spellParams.size - 1; radius >= 1; radius--) {
            List<Point> effectSquares = getBallArea(gs, visibleTarget, radius);
            events.add(GameEvent.Effect(new DungeonEffect(effectName, effectSquares)));
        }

        // hit everything in the large ball once
        List<Point> hitSquares = getBallArea(gs, visibleTarget, spellParams.size);
        AttackUtils.HitParams hitParams = new AttackUtils.HitParams(spellParams, actor);
        for (Point s : hitSquares) {
            Actor m = gs.getCurrentMap().actorAt(s.x, s.y);
            if (m != null) {
                events.addAll(AttackUtils.doHit(backend, actor, m, hitParams));
            }
        }

        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() {
        return true;
    }

    @Override
    public Actor getActor() {
        return actor;
    }

}
