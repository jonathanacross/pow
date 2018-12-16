package pow.backend.event;

import pow.backend.GameBackend;

import java.util.List;

public interface GameEvent {
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
        WAITING_USER_INPUT,
    }

    public List<GameEvent> process(GameBackend backend);
    public EventType getEventType();
    public boolean showUpdate();
}
