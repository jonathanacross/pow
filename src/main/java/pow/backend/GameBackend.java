package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.command.CommandRequest;
import pow.backend.event.GameEvent;

import java.util.*;

public class GameBackend {

    private GameState gameState;
    public Queue<CommandRequest> commandQueue = new LinkedList<>();
    private boolean logChanged;

    public GameState getGameState() {
        return gameState;
    }

    public GameBackend() {
        this.gameState = new GameState("Unknown Adventurer");
    }

    public List<GameEvent> processCommand() {
        List<GameEvent> events = new ArrayList<>();

        this.logChanged = false;
        if (! commandQueue.isEmpty()) {
            gameState.player.addCommand(commandQueue.poll());

            for (Actor a: gameState.map.actors) {
                events.addAll(a.act(this));
            }

            // TODO: this is a bad hack to make sure we don't modify the
            // array of actors while we're iterating through it -- e.g.,
            // because a monster/pet gets killed.  Right now, it's possible
            // that we kill a monster, and then it does its attack, and then
            // the monster is removed here.  :(
            for (Iterator<Actor> iterator = gameState.map.actors.iterator(); iterator.hasNext(); ) {
                Actor a = iterator.next();
                if (a.health < 0) {
                    events.add(GameEvent.KILLED);
                    iterator.remove();
                    if (a == gameState.pet) {
                        gameState.pet = null;
                    }
                }
            }
        }
        if (logChanged) {
            events.add(GameEvent.LOG_UPDATE);
        }

        return events;
    }

    // note: this must be the only way to log a message, otherwise it may
    // not immediately show up to the user.
    // TODO: is there a way to clean up the 'logChanged' variable?
    public void logMessage(String message) {
        logChanged = true;
        gameState.log.add(message);
    }

    public void newGame(String name) {
        gameState = new GameState(name);
    }

    public void load(GameState gameState) {
        this.gameState = gameState;
    }
}
