package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.event.GameEvent;

import java.util.List;

public abstract class CommandRequest {
    public abstract List<GameEvent> process(GameBackend backend);
}
