package pow.backend.event;

import pow.backend.actors.Actor;
import pow.util.Point;

// class to report back interesting things that the UI should show
public class GameEvent {

    public enum EventType {
        LOG_UPDATE,
        MOVED,  // player or creature moved
        ATTACKED,
        ARROW,
        KILLED,
        WON_GAME,
        LOST_GAME,
        IN_STORE,
        HEALED,
        DUNGEON_UPDATED,  // possibly make this more granular: dig, unlocked....
        ROCKET  // demo effect to show animation
    }

    // TODO: fill out this class.. what's needed? can I simplify?
    public EventType eventType;
    public Actor actor; // may be null for some events
    public Point point; // may be null for many events

    private GameEvent(EventType eventType, Actor actor, Point point) {
        this.eventType = eventType;
        this.actor = actor;
        this.point = point;
    }

    public static GameEvent LogUpdate() { return new GameEvent(EventType.LOG_UPDATE, null, null); }
    public static GameEvent Moved() { return new GameEvent(EventType.MOVED, null, null); }
    public static GameEvent Healed() { return new GameEvent(EventType.HEALED, null, null); }
    public static GameEvent Attacked() { return new GameEvent(EventType.ATTACKED, null, null); }
    public static GameEvent Arrow(Actor a, Point p) { return new GameEvent(EventType.ARROW, a, p); }
    public static GameEvent Killed() { return new GameEvent(EventType.KILLED, null, null); }
    public static GameEvent WonGame() { return new GameEvent(EventType.WON_GAME, null, null); }
    public static GameEvent LostGame() { return new GameEvent(EventType.LOST_GAME, null, null); }
    public static GameEvent InStore() { return new GameEvent(EventType.IN_STORE, null, null); }
    public static GameEvent DungeonUpdated() { return new GameEvent(EventType.DUNGEON_UPDATED, null, null); }
    public static GameEvent Rocket(Actor a) { return new GameEvent(EventType.ROCKET, a, null); }
}
