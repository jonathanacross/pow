package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

public interface CommandRequest {
    ActionResult process(GameBackend backend);
    boolean consumesEnergy();
    Actor getActor();
}
