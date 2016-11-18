package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.event.GameEvent;
import pow.frontend.save.SaveUtils;

import java.util.ArrayList;
import java.util.List;

public class Save implements CommandRequest {
    @Override
    public List<GameEvent> process(GameBackend backend) {
        SaveUtils.saveToFile(backend.getGameState());
        backend.logMessage("saved.");
        return new ArrayList<>();
    }
}
