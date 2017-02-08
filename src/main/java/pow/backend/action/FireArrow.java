package pow.backend.action;

import pow.backend.AttackData;
import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.event.GameEvent;
import pow.util.Bresenham;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class FireArrow implements Action {
    Actor attacker;
    Point target;

    private static final int FIRE_RANGE = 5; // how far can arrows be shot?

    public FireArrow(Actor attacker, Point target) {
        this.attacker = attacker;
        this.target = target;
    }
    public Actor getActor() {
        return this.attacker;
    }

    public static ActionResult doAttack(GameBackend backend, Actor attacker, Point target) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gs.getCurrentMap();

        // special case if it's the player who's attacking -- use their bow as the attack
        AttackData attackData = (attacker == gs.player) ?
                gs.player.bowAttack :
                attacker.attack;

        List<Point> ray = Bresenham.makeRay(gs.player.loc, target, FIRE_RANGE + 1);
        ray.remove(0); // remove the attacker from the path of the arrow.
        for (Point p : ray) {
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                boolean hitsTarget = gs.rng.nextDouble() > AttackUtils.hitProb(attackData.plusToHit, defender.defense);
                int damage = attacker.attack.dieRoll.rollDice(gs.rng) + attacker.attack.plusToDam;
                if (hitsTarget && damage > 0) {
                    backend.logMessage(attacker.getPronoun() + " hits " + defender.getPronoun());
                    List<GameEvent> hitEvents = AttackUtils.doHit(backend, attacker, defender, damage);
                    events.addAll(hitEvents);
                    break;
                }
            }
            if (map.map[p.x][p.y].blockAir()) break;
        }
        if (attacker == gs.player) {
            DungeonItem arrows = gs.player.findArrows();
            gs.player.inventory.removeOneItem(arrows);
        }

        return ActionResult.Succeeded(events);
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return doAttack(backend, this.attacker, this.target);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
