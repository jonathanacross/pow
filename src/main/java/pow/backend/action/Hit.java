package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.backend.utils.AttackUtils;

import java.util.ArrayList;
import java.util.List;

public class Hit implements Action {

    private final Actor attacker;
    private final Actor defender;
    private final AttackUtils.HitParams hitParams;

    public Hit(Actor attacker, Actor defender, AttackUtils.HitParams hitParams) {
        this.attacker = attacker;
        this.defender = defender;
        this.hitParams = hitParams;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>(AttackUtils.doHit(backend, attacker, defender, hitParams));
        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() {
        // This is called only as a subaction, so this should
        // be followed by another "CompletedAction" to consume energy.
        return false;
    }

    @Override
    public Actor getActor() {
        return attacker;
    }
}
