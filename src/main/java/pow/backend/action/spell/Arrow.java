package pow.backend.action.spell;

import pow.backend.*;
import pow.backend.action.Action;
import pow.backend.action.ActionResult;
import pow.backend.action.AttackUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class Arrow implements Action, Spell {
    private final Actor attacker;
    private final Point target;

    public Arrow(Actor attacker, Point target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public int getRequiredMana() { return 1; }

    @Override
    public Actor getActor() {
        return this.attacker;
    }

    // TODO: remove duplicated code with FireArrow
    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gs.getCurrentMap();

        AttackData attackData = attacker.getSecondaryAttack();

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, GameConstants.ACTOR_ARROW_FIRE_RANGE + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.ARROW,
                DungeonEffect.EffectColor.NONE,
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        for (Point p : ray) {
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                boolean hitsTarget = gs.rng.nextDouble() > AttackUtils.hitProb(attackData.plusToHit, defender.getDefense());
                int damage = attacker.level; // TODO: tune this formula
                if (hitsTarget && damage > 0) {
                    backend.logMessage(attacker.getPronoun() + " hits " + defender.getPronoun());
                    List<GameEvent> hitEvents = AttackUtils.doHit(backend, attacker, defender, damage);
                    events.addAll(hitEvents);
                    break;
                }
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
            events.add(GameEvent.Effect(new DungeonEffect(effectId, p)));
        }
        attacker.useMana(getRequiredMana());
        events.add(GameEvent.DungeonUpdated());

        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
