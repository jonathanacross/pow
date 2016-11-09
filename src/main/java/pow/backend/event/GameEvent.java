package pow.backend.event;

// class to report back interesting things that the UI should show
public enum GameEvent {
    MOVED,  // player or creature moved
    ATTACKED,
    KILLED,
    WON_GAME,
    LOST_GAME,
    IN_STORE,
    ROCKET,  // demo effect to show animation
}
