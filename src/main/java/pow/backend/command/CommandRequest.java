package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.event.GameEvent;

import java.util.List;

public interface CommandRequest {
    List<GameEvent> process(GameBackend backend);
}
