package pow.backend.event;

import pow.backend.dungeon.DungeonEffect;

// class to report back interesting things that the UI should show
public class GameEvent {

    // TODO: collapse some of these (e.g., moved, healed, log_update?) into dungeon_updated
    // Types marked with a * require special processing by frontend. Others may be good
    // candidates for collapsing.
    public enum EventType {
        LOG_UPDATE,
        MOVED,
        ATTACKED,
        KILLED,
        WON_GAME,  // * triggers window to show game won
        LOST_GAME, // * triggers window to show game lost
        GOT_PET,   // * triggers window to choose pet
        IN_STORE,  // * triggers window to buy item
        HEALED,
        DUNGEON_UPDATED,
        EFFECT,    // * triggers delay while drawing
        IN_PORTAL, // * triggers window to choose portal
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
