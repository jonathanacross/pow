package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.frontend.save.SaveUtils;

import java.util.ArrayList;
import java.util.List;

public class Save implements CommandRequest {
    @Override
    public ActionResult process(GameBackend backend) {
        SaveUtils.saveToFile(backend.getGameState());
        backend.logMessage("saved.");
        return ActionResult.Succeeded(new ArrayList<>());
    }

    @Override
    public Actor getActor() {
        return null;
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
