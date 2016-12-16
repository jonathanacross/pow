package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.command.CommandRequest;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
