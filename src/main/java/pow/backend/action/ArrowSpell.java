package pow.backend.action;

import pow.backend.*;
import pow.backend.utils.AttackUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

// Spell that casts various types of arrows.  There's a fair amount of code
// in common with the Arrow Action, but the parameters for hitting, doing damage,
// and losing physical arrows are different enough that it's not worth combining
// them.
public class ArrowSpell implements Action {
    private final Actor attacker;
    private final Point target;
    private final SpellParams spellParams;

    public ArrowSpell(Actor attacker, Point target, SpellParams spellParams) {
        this.attacker = attacker;
        this.target = target;
        this.spellParams = spellParams;
    }

    @Override
    public Actor getActor() {
        return this.attacker;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gs.getCurrentMap();

        backend.logMessage(attacker.getNoun() + " fire an arrow.", MessageLog.MessageType.COMBAT_NEUTRAL);

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, spellParams.size + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.ARROW,
                DungeonEffect.EffectColor.NONE,
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        AttackUtils.HitParams hitParams = new AttackUtils.HitParams(spellParams, attacker, backend.getGameState().rng);
        for (Point p : ray) {
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                events.addAll(AttackUtils.doHit(backend, attacker, defender, hitParams));
                break;
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
            events.add(GameEvent.Effect(new DungeonEffect(effectId, p)));
        }

        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
