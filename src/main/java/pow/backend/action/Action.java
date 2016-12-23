package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

public interface Action {
    ActionResult process(GameBackend backend);
    boolean consumesEnergy();
    Actor getActor();
}
