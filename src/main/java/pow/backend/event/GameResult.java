package pow.backend.event;

import java.util.List;

public class GameResult {
    public boolean madeProgress; // TODO: not sure this is needed
    public List<GameEvent> events;

    public GameResult(boolean madeProgress, List<GameEvent> events) {
        this.madeProgress = madeProgress;
        this.events = events;
    }
}
