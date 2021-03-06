package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.frontend.utils.SaveUtils;

import java.util.Collections;

public class Save implements Action {
    @Override
    public ActionResult process(GameBackend backend) {
        SaveUtils.saveToFile(backend.getGameState());
        backend.logMessage("saved.", MessageLog.MessageType.GAME_EVENT);
        return ActionResult.succeeded(Collections.emptyList());
    }

    @Override
    public Actor getActor() {
        return null;
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
