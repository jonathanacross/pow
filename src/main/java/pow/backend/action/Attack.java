package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Attack implements Action {
    private final Actor attacker;
    private final Actor target;

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
        if (gs.rng.nextDouble() > AttackUtils.hitProb(attacker.getPrimaryAttack().plusToHit, target.getDefense())) {
            backend.logMessage(attacker.getPronoun() + " misses " + target.getPronoun());
        } else {
            int damage = attacker.getPrimaryAttack().dieRoll.rollDice(gs.rng) + attacker.getPrimaryAttack().plusToDam;
            if (damage == 0) {
                backend.logMessage(attacker.getPronoun() + " misses " + target.getPronoun());
            } else {
                events.addAll(AttackUtils.doHit(backend, attacker, target, new AttackUtils.HitParams(damage)));
            }
        }
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
