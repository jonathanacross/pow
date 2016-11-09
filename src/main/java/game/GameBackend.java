package game;

import game.backend.command.CommandRequest;
import game.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameBackend {
    public GameState getGameState() {
        return gameState;
    }

    private GameState gameState;
    public Queue<CommandRequest> commandQueue = new LinkedList<>();

    public GameBackend() {
        this.gameState = new GameState();
    }

    public List<GameEvent> processCommand() {
        List<GameEvent> events = new ArrayList<>();

        if (! commandQueue.isEmpty()) {
            events.addAll(commandQueue.poll().process(this));
        }

        return events;
    }

    public void newGame() {
        gameState = new GameState();
    }

    public void load(GameState gameState) {
        gameState = gameState;
    }
}
