package pow.backend.action;

import pow.backend.*;
import pow.backend.event.Effect;
import pow.backend.event.GameEvent;
import pow.backend.event.Hit;
import pow.backend.utils.AttackUtils;
import pow.backend.utils.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.util.*;

import java.util.*;

public class ChainSpell implements Action {

    private final Actor attacker;
    private final SpellParams spellParams;

    public ChainSpell(Actor attacker, SpellParams spellParams) {
        this.attacker = attacker;
        this.spellParams = spellParams;
    }

    @Override
    public Actor getActor() {
        return this.attacker;
    }

    private Point findTarget(GameState gameState, Point center, int radius, Set<Point> excludedPoints) {
        GameMap map = gameState.getCurrentMap();
        Point target = null;
        List<Point> possibleTargets = SpellUtils.getFieldOfView(gameState, center, radius, Metric.euclideanMetric);
        for (Point p : possibleTargets) {
            if (excludedPoints.contains(p)) continue;
            Actor actor = map.actorAt(p.x, p.y);
            if (actor == null) continue;
            if (actor.friendly == attacker.friendly) continue;
            if ((target == null) || (MathUtils.dist2(center, target) > MathUtils.dist2(center, p))) {
                target = p;
            }
        }
        return target;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gs.getCurrentMap();

        backend.logMessage(attacker.getNoun() + " casts a" +
                AttackUtils.getDamageTypeString(spellParams.element) + " chain", MessageLog.MessageType.COMBAT_NEUTRAL);

        Set<Point> excluded = new HashSet<>();
        Point curr = attacker.loc;
        for (int i = 0; i < 10; i++) {  // unlikely there will ever be this many monsters, but if so..
            Point target = findTarget(gs, curr, spellParams.size, excluded);
            if (target == null) {
                break;
            }

            List<Point> ray = Bresenham.makeRay(curr, target, spellParams.size + 1);
            String effectId = DungeonEffect.getEffectName(
                    DungeonEffect.EffectType.BOLT,
                    SpellUtils.getEffectColor(spellParams.element),
                    Direction.getDir(curr, target));

            ray.remove(0); // remove the attacker from the path
            AttackUtils.HitParams hitParams = new AttackUtils.HitParams(spellParams, attacker, backend.getGameState().rng);
            for (Point p : ray) {
                Actor defender = map.actorAt(p.x, p.y);
                if (defender != null) {
                    events.add(new Hit(attacker, defender, hitParams));
                    excluded.add(defender.loc);
                    break;
                }
                if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
                if (map.map[p.x][p.y].blockAir()) break;
                events.add(new Effect(new DungeonEffect(effectId, p)));
            }

            curr = target;
        }
        // clear out last effect.
        // TODO: should this be new dungeonupdated?
        events.add(new Effect(new DungeonEffect(Collections.emptyList())));

        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
