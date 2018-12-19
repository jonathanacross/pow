package pow.backend.action;

import pow.backend.*;
import pow.backend.utils.AttackUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.DungeonItem;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
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
        List<Action> subactions = new ArrayList<>();
        GameMap map = gs.getCurrentMap();

        backend.logMessage(attacker.getNoun() + " fires an arrow.", MessageLog.MessageType.COMBAT_NEUTRAL);

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, GameConstants.ACTOR_ARROW_FIRE_RANGE + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.ARROW,
                DungeonEffect.EffectColor.NONE,
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        for (Point p : ray) {
            subactions.add(new ShowEffect(new DungeonEffect(effectId, p)));
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                boolean hitsTarget = gs.rng.nextDouble() <= AttackUtils.hitProb(attackData.plusToHit, defender.getDefense());
                int damage = attackData.dieRoll.rollDice(gs.rng) + attackData.plusToDam;
                if (hitsTarget && damage > 0) {
                    AttackUtils.HitParams hitParams = new AttackUtils.HitParams(damage);
                    subactions.add(new Hit(attacker, defender, hitParams));
                    break;
                }
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
        }

        if (attacker == gs.party.player) {
            DungeonItem arrows = gs.party.player.findArrows();
            int count = arrows.count - 1;
            gs.party.player.inventory.removeOneItem(arrows);
            backend.logMessage(gs.party.player.getNoun() + " has " + count + " arrows left", MessageLog.MessageType.GENERAL);
        }

        // clear out last effect.
        // TODO: should this be new dungeonupdated?
        subactions.add(new ShowEffect(new DungeonEffect(Collections.emptyList())));
        //events.add(GameEventOld.DungeonUpdated());
        return ActionResult.failed(subactions);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
