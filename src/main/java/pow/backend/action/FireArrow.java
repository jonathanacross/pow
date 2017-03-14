package pow.backend.action;

import pow.backend.AttackData;
import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.util.Bresenham;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class FireArrow implements Action {
    private final Actor attacker;
    private final Point target;

    private static final int FIRE_RANGE = 6; // how far can arrows be shot?

    public FireArrow(Actor attacker, Point target) {
        this.attacker = attacker;
        this.target = target;
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

        AttackData attackData = attacker.getSecondaryAttack();

        List<Point> ray = Bresenham.makeRay(gs.player.loc, target, FIRE_RANGE + 1);
        ray.remove(0); // remove the attacker from the path of the arrow.
        Point lastPoint = null;
        for (Point p : ray) {
            lastPoint = p;
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                boolean hitsTarget = gs.rng.nextDouble() > AttackUtils.hitProb(attackData.plusToHit, defender.getDefense());
                int damage = attackData.dieRoll.rollDice(gs.rng) + attackData.plusToDam;
                if (hitsTarget && damage > 0) {
                    backend.logMessage(attacker.getPronoun() + " hits " + defender.getPronoun());
                    List<GameEvent> hitEvents = AttackUtils.doHit(backend, attacker, defender, damage);
                    events.addAll(hitEvents);
                    break;
                }
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
        }
        events.add(GameEvent.Arrow(attacker, lastPoint));
        if (attacker == gs.player) {
            DungeonItem arrows = gs.player.findArrows();
            int count = arrows.count - 1;
            gs.player.inventory.removeOneItem(arrows);
            backend.logMessage("you have " + count + " arrows left");
        }

        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
