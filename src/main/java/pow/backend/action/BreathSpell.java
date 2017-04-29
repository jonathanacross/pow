package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Direction;
import pow.util.Metric;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

import static pow.backend.action.SpellUtils.getFieldOfView;

public class BreathSpell implements Action {

    private final Actor actor;
    private final Point target;
    private final SpellParams spellParams;

    public BreathSpell(Actor actor, Point target, SpellParams spellParams) {
        this.actor = actor;
        this.target = target;
        this.spellParams = spellParams;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();

        // draw effects
        String effectName = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.LARGE_BALL,
                SpellUtils.getEffectColor(spellParams.element),
                Direction.N); // dummy
        for (int radius = 1; radius <= spellParams.size; radius++) {
            List<Point> effectSquares = getBreathArea(gs, actor.loc, target, radius);
            events.add(GameEvent.Effect(new DungeonEffect(effectName, effectSquares)));
        }

        // hit everything in the large area once
        List<Point> hitSquares = getBreathArea(gs, actor.loc, target, spellParams.size);
        int damage = spellParams.getAmount(actor);

        for (Point s : hitSquares) {
            Actor m = gs.getCurrentMap().actorAt(s.x, s.y);
            if (m != null) {
                events.addAll(AttackUtils.doHit(backend, actor, m, spellParams.element, damage));
            }
        }

        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    private static List<Point> getBreathArea(GameState gameState, Point center, Point target, int radius) {
        final double cosThreshold = 0.866025403784439; // = cos(pi/6); results in a breath of angle pi/3

        List<Point> ballSquares = getFieldOfView(gameState, center, radius, Metric.euclideanMetric);
        List<Point> breathSquares = new ArrayList<>();

        Point centerToTarget = new Point(target.x - center.x, target.y - center.y);
        for (Point s : ballSquares) {
            if (s == center) continue;

            Point centerToSquare = new Point(s.x - center.x, s.y - center.y);
            int dot = centerToTarget.dot(centerToSquare);
            double normCenterToTarget = Math.sqrt(centerToTarget.dot(centerToTarget));
            double normCenterToSquare = Math.sqrt(centerToSquare.dot(centerToSquare));
            double cosTheta = (double) dot / (normCenterToSquare * normCenterToTarget);
            if (cosTheta >= cosThreshold) {
                breathSquares.add(s);
            }
        }

        return breathSquares;
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
