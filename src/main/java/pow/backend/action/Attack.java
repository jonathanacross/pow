package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.ItemGenerator;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Attack implements Action {
    Actor attacker;
    Actor target;

    public Attack(Actor attacker, Actor target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public Actor getActor() {
        return this.attacker;
    }

    public static ActionResult doAttack(GameBackend backend, Actor attacker, Actor defender) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        if (gs.rng.nextDouble() > AttackUtils.hitProb(attacker.attack.plusToHit, defender.defense)) {
            backend.logMessage(attacker.getPronoun() + " misses " + defender.getPronoun());
        } else {
            int damage = attacker.attack.dieRoll.rollDice(gs.rng) + attacker.attack.plusToDam;
            if (damage == 0) {
                backend.logMessage(attacker.getPronoun() + " misses " + defender.getPronoun());
            } else {
                List<GameEvent> hitEvents = AttackUtils.doHit(backend, attacker, defender, damage);
                events.addAll(hitEvents);
            }
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
