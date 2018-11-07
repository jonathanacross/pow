package pow.backend.event;

import pow.backend.dungeon.DungeonEffect;

// class to report back interesting things that the UI should show
public class GameEvent {

    // TODO: collapse some of these (e.g., moved, healed, log_update?) into dungeon_updated
    public enum EventType {
        LOG_UPDATE,
        MOVED,  // player or creature moved
        ATTACKED,
        KILLED,
        WON_GAME,
        LOST_GAME,
        GOT_PET,
        IN_STORE,
        HEALED,
        DUNGEON_UPDATED,  // indicates UI needs a redraw
        EFFECT,
        IN_PORTAL,
    }

    public final EventType eventType;
    public final DungeonEffect effect;

    private GameEvent(EventType eventType, DungeonEffect effect) {
        this.eventType = eventType;
        this.effect = effect;
    }

    public static GameEvent LogUpdate() { return new GameEvent(EventType.LOG_UPDATE, null); }
    public static GameEvent Moved() { return new GameEvent(EventType.MOVED, null); }
    public static GameEvent Healed() { return new GameEvent(EventType.HEALED, null); }
    public static GameEvent Attacked() { return new GameEvent(EventType.ATTACKED, null); }
    public static GameEvent Killed() { return new GameEvent(EventType.KILLED, null); }
    public static GameEvent WonGame() { return new GameEvent(EventType.WON_GAME, null); }
    public static GameEvent LostGame() { return new GameEvent(EventType.LOST_GAME, null); }
    public static GameEvent GotPet() { return new GameEvent(EventType.GOT_PET, null); }
    public static GameEvent InStore() { return new GameEvent(EventType.IN_STORE, null); }
    public static GameEvent InPortal() { return new GameEvent(EventType.IN_PORTAL, null); }
    public static GameEvent DungeonUpdated() { return new GameEvent(EventType.DUNGEON_UPDATED, null); }
    public static GameEvent Effect(DungeonEffect effect) { return new GameEvent(EventType.EFFECT, effect); }
}
