package pow.backend.event;

import java.util.List;

public class GameResult {
    public List<GameEvent> events;

    public GameResult(List<GameEvent> events) {
        this.events = events;
    }

    public void addEvents(List<GameEvent> events) {
        this.events.addAll(events);
    }
}
