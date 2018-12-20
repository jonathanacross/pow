package pow.backend.event;

import java.util.List;

public class GameResult {
    public final List<GameEvent> events;

    public GameResult(List<GameEvent> events) {
        this.events = events;
    }
}
