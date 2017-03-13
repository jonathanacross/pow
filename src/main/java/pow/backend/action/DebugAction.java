package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

// class to store all the debugging actions, so they are all in one place.
public class DebugAction implements Action {

    private final What what;

    public enum What {
        INCREASE_CHAR_LEVEL,
        HEAL
    }

    public DebugAction(What what) {
        this.what = what;
    }

    @Override
    public Actor getActor() { return null; }

    @Override
    public ActionResult process(GameBackend backend) {
        Player player = backend.getGameState().player;

        switch (what) {
            case INCREASE_CHAR_LEVEL:
                if (player.level < 20) {
                    backend.logMessage("DEBUG: player up a level");
                    int expNeeded = player.getExpToNextLevel();
                    player.gainExperience(backend, expNeeded + 1);
                }
                break;
            case HEAL:
                backend.logMessage("DEBUG: healing player");
                player.setFullHealth();
                player.setFullMana();
                break;
        }
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}

