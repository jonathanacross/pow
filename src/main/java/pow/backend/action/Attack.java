package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.conditions.ConditionTypes;
import pow.backend.event.GameEvent;
import pow.backend.event.Hit;
import pow.backend.utils.AttackUtils;
import pow.backend.actors.Actor;

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
            backend.logMessage(attacker.getNoun() + " misses " + target.getNoun(),
                    MessageLog.MessageType.COMBAT_NEUTRAL);
        } else {
            int damage = attacker.getPrimaryAttack().dieRoll.rollDice(gs.rng) + attacker.getPrimaryAttack().plusToDam;
            if (damage == 0) {
                backend.logMessage(attacker.getNoun() + " misses " + target.getNoun(),
                        MessageLog.MessageType.COMBAT_NEUTRAL);
            } else {
                // handle special abilities
                if (attacker.abilities.poisonDamage && gs.rng.nextInt(8) == 0) {
                    int duration = 10;
                    int intensity = (int) Math.ceil(0.1 * damage);
                    events.addAll(target.conditions.get(ConditionTypes.POISON).start(backend, duration, intensity, attacker));
                }
                if (attacker.abilities.stunDamage && gs.rng.nextInt(8) == 0) {
                    int duration = 10;
                    int intensity = (int) Math.ceil(0.2 * damage);
                    events.addAll(target.conditions.get(ConditionTypes.STUN).start(backend, duration, intensity, attacker));
                }

                events.add(new Hit(attacker, target, new AttackUtils.HitParams(damage)));
            }
        }
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
