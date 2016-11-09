package game.backend.command;

import game.GameBackend;
import game.backend.event.GameEvent;

import java.util.List;

public abstract class CommandRequest {
    public abstract List<GameEvent> process(GameBackend backend);
}
