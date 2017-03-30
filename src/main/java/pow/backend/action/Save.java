package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.frontend.utils.SaveUtils;

import java.util.ArrayList;

public class Save implements Action {
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
