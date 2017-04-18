package pow.backend.action;

import pow.backend.*;
import pow.backend.action.spell.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class BoltSpell implements Action {

    private final Actor attacker;
    private final Point target;
    private final SpellParams spellParams;

    public BoltSpell(Actor attacker, Point target, SpellParams spellParams) {
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

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, spellParams.size + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.SMALL_BALL,
                SpellUtils.getEffectColor(spellParams.element),
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        for (Point p : ray) {
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                int damage = spellParams.getAmount(attacker);
                backend.logMessage(attacker.getPronoun() + " hits " + defender.getPronoun());
                List<GameEvent> hitEvents = AttackUtils.doHit(backend, attacker, defender, damage);
                events.addAll(hitEvents);
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
