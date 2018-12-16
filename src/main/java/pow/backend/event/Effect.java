package pow.backend.event;

import pow.backend.GameBackend;
import pow.backend.dungeon.DungeonEffect;

import java.util.Collections;
import java.util.List;

// TODO: rename? this is confusing since there's also a DungeonEffect
public class Effect implements GameEvent {

    private DungeonEffect effect;

    public Effect(DungeonEffect effect) {
        this.effect = effect;
    }

    @Override
    public List<GameEvent> process(GameBackend backend) {
        backend.getGameState().getCurrentMap().effects.clear();
        backend.getGameState().getCurrentMap().effects.add(this.effect);
        return Collections.emptyList();
    }

    @Override
    public EventType getEventType() {
        return EventType.EFFECT;
    }

    @Override
    public boolean showUpdate() { return true; }
}
