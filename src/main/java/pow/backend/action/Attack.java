package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Attack implements Action {
    private Actor attacker;
    private Actor target;

    public Attack(Actor attacker, Actor target) {
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
        if (gs.rng.nextDouble() > AttackUtils.hitProb(attacker.attack.plusToHit, target.defense)) {
            backend.logMessage(attacker.getPronoun() + " misses " + target.getPronoun());
        } else {
            int damage = attacker.attack.dieRoll.rollDice(gs.rng) + attacker.attack.plusToDam;
            if (damage == 0) {
                backend.logMessage(attacker.getPronoun() + " misses " + target.getPronoun());
            } else {
                List<GameEvent> hitEvents = AttackUtils.doHit(backend, attacker, target, damage);
                events.addAll(hitEvents);
            }
        }
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
