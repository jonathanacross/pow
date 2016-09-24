package game;

import java.io.Serializable;

public class GameState implements Serializable {

    private static GameState instance = null;

    public static GameState getInstance() {
        if (instance == null) {
            synchronized (GameState.class) {
                if (instance == null) {
                    instance = new GameState();
                }
            }
        }
        return instance;
    }

    public static void newGame() {
        synchronized (GameState.class) {
            instance = new GameState();
        }
    }

    public static void load(GameState gameState) {
        synchronized (GameState.class) {
            instance = gameState;
        }
    }

    private GameState() {
        x = 50;
        y = 50;
    }

    public int x;
    public int y;

    public void moveRight() {
        x = x + 5;
    }
    public void moveLeft() {
        x = x - 5;
    }
    public void moveDown() {
        y = y + 5;
    }
    public void moveUp() {
        y = y - 5;
    }
}
