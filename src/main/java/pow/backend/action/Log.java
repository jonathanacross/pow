package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEventOld;

import java.util.Collections;

public class Log implements Action {

    public Log() { }

    @Override
    public ActionResult process(GameBackend backend) {
        return ActionResult.Succeeded(Collections.singletonList(GameEventOld.LogUpdate()));
    }

    @Override
    public Actor getActor() { return null; }

    @Override
    public boolean consumesEnergy() { return false; }
}
