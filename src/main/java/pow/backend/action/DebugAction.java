package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

// class to store all the debugging actions, so they are all in one place.
public class DebugAction implements Action {

    private final What what;

    public enum What {
        UNKNOWN,
        INCREASE_CHAR_LEVEL,
        HEAL,
        CLOSED_PORTAL_DEBUG,
        OPEN_PORTAL_DEBUG
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
            case UNKNOWN:
                backend.logMessage("DEBUG: unknown action", MessageLog.MessageType.DEBUG);
                break;
            case INCREASE_CHAR_LEVEL:
                if (player.level < 20) {
                    backend.logMessage("DEBUG: player up a level", MessageLog.MessageType.DEBUG);
                    int expNeeded = player.getExpToNextLevel();
                    player.gainExperience(backend, expNeeded + 1, null);
                }
                break;
            case HEAL:
                backend.logMessage("DEBUG: healing player", MessageLog.MessageType.DEBUG);
                player.setFullHealth();
                player.setFullMana();
                break;
            case CLOSED_PORTAL_DEBUG:
                backend.logMessage("DEBUG: hit closed portal", MessageLog.MessageType.DEBUG);
                break;
            case OPEN_PORTAL_DEBUG:
                backend.logMessage("DEBUG: hit open portal", MessageLog.MessageType.DEBUG);
                break;
        }
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }
}

