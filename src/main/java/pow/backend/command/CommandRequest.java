package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.List;

public interface CommandRequest {
    ActionResult process(GameBackend backend);
    boolean consumesEnergy();
    Actor getActor();
}
