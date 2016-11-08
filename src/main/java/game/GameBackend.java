package game;

import game.backend.command.CommandRequest;

import java.util.LinkedList;
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

    public void processCommand() {
        if (! commandQueue.isEmpty()) {
            commandQueue.poll().process(this);
        }
    }

    public void newGame() {
        gameState = new GameState();
    }

    public void load(GameState gameState) {
        gameState = gameState;
    }
}
