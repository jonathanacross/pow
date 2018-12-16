package pow.backend.event;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.utils.AttackUtils;

import java.util.Collections;
import java.util.List;

public class Hit implements GameEvent {

    private Actor attacker;
    private Actor defender;
    private AttackUtils.HitParams hitParams;

    public Hit(Actor attacker, Actor defender, AttackUtils.HitParams hitParams) {
        this.attacker = attacker;
        this.defender = defender;
        this.hitParams = hitParams;
    }

    @Override
    public List<GameEvent> process(GameBackend backend) {
        AttackUtils.doHit(backend, attacker, defender, hitParams);
        return Collections.emptyList();
    }

    @Override
    public EventType getEventType() {
        return EventType.DUNGEON_UPDATED;
    }

    @Override
    public boolean showUpdate() { return false; }
}
