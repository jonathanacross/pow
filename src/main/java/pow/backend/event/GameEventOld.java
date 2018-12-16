package pow.backend.event;

import pow.backend.GameBackend;
import pow.backend.dungeon.DungeonEffect;

import java.util.Collections;
import java.util.List;

// This is a temporary class until I get everything translated.
public class GameEventOld implements GameEvent {

    @Override
    public List<GameEvent> process(GameBackend backend) {
        return Collections.emptyList();
    }

    @Override
    public GameEvent.EventType getEventType() {
        return eventType;
    }

    @Override
    public boolean showUpdate() { return showUpdate; }

    public final GameEvent.EventType eventType;
    public final DungeonEffect effect;
    public final boolean showUpdate;

    private GameEventOld(GameEvent.EventType eventType, DungeonEffect effect) {
        this.eventType = eventType;
        this.effect = effect;
        this.showUpdate = false;
    }

    private GameEventOld(GameEvent.EventType eventType, DungeonEffect effect, boolean showUpdate) {
        this.eventType = eventType;
        this.effect = effect;
        this.showUpdate = showUpdate;
    }

    public static GameEvent LogUpdate() { return new GameEventOld(GameEvent.EventType.LOG_UPDATE, null); }
    public static GameEvent Moved() { return new GameEventOld(GameEvent.EventType.MOVED, null); }
    public static GameEvent Healed() { return new GameEventOld(GameEvent.EventType.HEALED, null); }
    public static GameEvent Attacked() { return new GameEventOld(GameEvent.EventType.ATTACKED, null); }
    public static GameEvent Killed() { return new GameEventOld(GameEvent.EventType.KILLED, null); }
    public static GameEvent WonGame() { return new GameEventOld(GameEvent.EventType.WON_GAME, null); }
    public static GameEvent LostGame() { return new GameEventOld(GameEvent.EventType.LOST_GAME, null); }
    public static GameEvent GotPet() { return new GameEventOld(GameEvent.EventType.GOT_PET, null); }
    public static GameEvent InStore() { return new GameEventOld(GameEvent.EventType.IN_STORE, null); }
    public static GameEvent InPortal() { return new GameEventOld(GameEvent.EventType.IN_PORTAL, null); }
    public static GameEvent DungeonUpdated() { return new GameEventOld(GameEvent.EventType.DUNGEON_UPDATED, null); }
    public static GameEvent Effect(DungeonEffect effect) { return new GameEventOld(GameEvent.EventType.EFFECT, effect); }
    public static GameEvent UserInput() { return new GameEventOld(EventType.WAITING_USER_INPUT, null, true); }
}
