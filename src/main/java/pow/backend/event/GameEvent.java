package pow.backend.event;

public enum GameEvent {
   LOG_UPDATE(false),
   MOVED(false),
   ATTACKED(false),
   KILLED(false),
   WON_GAME(false),  // triggers window to show game won
   LOST_GAME(false), // triggers window to show game lost
   GOT_PET(false),   // triggers window to choose pet
   IN_STORE(false),  // triggers window to buy item
   HEALED(false),
   DUNGEON_UPDATED(false),
   EFFECT(true),    // triggers delay while drawing
   IN_PORTAL(false), // triggers window to choose portal
   WAITING_USER_INPUT(true),
   UPDATE_NEED_REDRAW(true);

    private final boolean showUpdate;

    GameEvent(boolean showUpdate) {
        this.showUpdate = showUpdate;
    }

    public boolean showUpdate() {
        return this.showUpdate;
    }
}
