package pow.backend.action;

import pow.backend.*;
import pow.backend.action.Action;
import pow.backend.action.ActionResult;
import pow.backend.action.AttackUtils;
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
    // Distinguish between firing a real arrow and a magical one.
    private final boolean usePhysicalArrow;

    public Arrow(Actor attacker, Point target, AttackData attackData, boolean usePhysicalArrow) {
        this.attacker = attacker;
        this.target = target;
        this.attackData = attackData;
        this.usePhysicalArrow = usePhysicalArrow;
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

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, GameConstants.ACTOR_ARROW_FIRE_RANGE + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.ARROW,
                DungeonEffect.EffectColor.NONE,
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        for (Point p : ray) {
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                // TODO: for magical arrows, always hit?
                boolean hitsTarget = gs.rng.nextDouble() > AttackUtils.hitProb(attackData.plusToHit, defender.getDefense());
                int damage = attackData.dieRoll.rollDice(gs.rng) + attackData.plusToDam;
                if (hitsTarget && damage > 0) {
                    backend.logMessage(attacker.getPronoun() + " hits " + defender.getPronoun());
                    events.addAll(AttackUtils.doHit(backend, attacker, defender, damage));
                    break;
                }
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
            events.add(GameEvent.Effect(new DungeonEffect(effectId, p)));
        }

        if (usePhysicalArrow && (attacker == gs.player)) {
            DungeonItem arrows = gs.player.findArrows();
            int count = arrows.count - 1;
            gs.player.inventory.removeOneItem(arrows);
            backend.logMessage("you have " + count + " arrows left");
        }

        events.add(GameEvent.DungeonUpdated());

        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
