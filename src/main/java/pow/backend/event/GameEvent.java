package pow.backend.event;

import pow.backend.actors.Actor;

// class to report back interesting things that the UI should show
public class GameEvent {

    public enum EventType {
        LOG_UPDATE,
        MOVED,  // player or creature moved
        ATTACKED,
        KILLED,
        WON_GAME,
        LOST_GAME,
        IN_STORE,
        ROCKET;  // demo effect to show animation
    }

    // TODO: fill out this class.. what's needed? can I simplify?
    public EventType eventType;
    public Actor actor; // may be null for some events

    public GameEvent(EventType eventType, Actor actor) {
        this.eventType = eventType;
        this.actor = actor;
    }

    public static GameEvent LogUpdate() { return new GameEvent(EventType.LOG_UPDATE, null); }
    public static GameEvent Moved() { return new GameEvent(EventType.MOVED, null); }
    public static GameEvent Attacked() { return new GameEvent(EventType.ATTACKED, null); }
    public static GameEvent Killed() { return new GameEvent(EventType.KILLED, null); }
    public static GameEvent WonGame() { return new GameEvent(EventType.WON_GAME, null); }
    public static GameEvent LostGame() { return new GameEvent(EventType.LOST_GAME, null); }
    public static GameEvent InStore() { return new GameEvent(EventType.IN_STORE, null); }
    public static GameEvent Rocket(Actor a) { return new GameEvent(EventType.ROCKET, a); }
}
