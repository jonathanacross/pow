package pow.backend.event;

import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.util.Point;

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
        HEALED,
        DUNGEON_UPDATED,  // possibly make this more granular: dig, unlocked....
        EFFECT
    }

    // TODO: fill out this class.. what's needed? can I simplify?
    public final EventType eventType;
    // TODO: see if these two fields can be removed.
    public final Actor actor; // may be null for some events
    public final Point point; // may be null for many events
    public final DungeonEffect effect;

    private GameEvent(EventType eventType, Actor actor, Point point, DungeonEffect effect) {
        this.eventType = eventType;
        this.actor = actor;
        this.point = point;
        this.effect = effect;
    }

    public static GameEvent LogUpdate() { return new GameEvent(EventType.LOG_UPDATE, null, null, null); }
    public static GameEvent Moved() { return new GameEvent(EventType.MOVED, null, null, null); }
    public static GameEvent Healed() { return new GameEvent(EventType.HEALED, null, null, null); }
    public static GameEvent Attacked() { return new GameEvent(EventType.ATTACKED, null, null, null); }
    public static GameEvent Killed() { return new GameEvent(EventType.KILLED, null, null, null); }
    public static GameEvent WonGame() { return new GameEvent(EventType.WON_GAME, null, null, null); }
    public static GameEvent LostGame() { return new GameEvent(EventType.LOST_GAME, null, null, null); }
    public static GameEvent InStore() { return new GameEvent(EventType.IN_STORE, null, null, null); }
    public static GameEvent DungeonUpdated() { return new GameEvent(EventType.DUNGEON_UPDATED, null, null, null); }
    public static GameEvent Effect(DungeonEffect effect) { return new GameEvent(EventType.EFFECT, null, null, effect); }
}
