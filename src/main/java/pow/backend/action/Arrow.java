package pow.backend.action;

import pow.backend.*;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class Arrow implements Action {
    private final Actor attacker;
    private final Point target;
    private final AttackData attackData;

    public Arrow(Actor attacker, Point target, AttackData attackData) {
        this.attacker = attacker;
        this.target = target;
        this.attackData = attackData;
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

        backend.logMessage(attacker.getPronoun() + " fire an arrow.", MessageLog.MessageType.COMBAT_NEUTRAL);

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, GameConstants.ACTOR_ARROW_FIRE_RANGE + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.ARROW,
                DungeonEffect.EffectColor.NONE,
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        for (Point p : ray) {
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                boolean hitsTarget = gs.rng.nextDouble() <= AttackUtils.hitProb(attackData.plusToHit, defender.getDefense());
                int damage = attackData.dieRoll.rollDice(gs.rng) + attackData.plusToDam;
                if (hitsTarget && damage > 0) {
                    AttackUtils.HitParams hitParams = new AttackUtils.HitParams(damage);
                    events.addAll(AttackUtils.doHit(backend, attacker, defender, hitParams));
                    break;
                }
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
            events.add(GameEvent.Effect(new DungeonEffect(effectId, p)));
        }

        if (attacker == gs.player) {
            DungeonItem arrows = gs.player.findArrows();
            int count = arrows.count - 1;
            gs.player.inventory.removeOneItem(arrows);
            backend.logMessage("you have " + count + " arrows left", MessageLog.MessageType.GENERAL);
        }

        events.add(GameEvent.DungeonUpdated());

        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
