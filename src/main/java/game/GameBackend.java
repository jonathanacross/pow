package game;

import java.util.ArrayList;
import java.util.List;
import util.Observer;

public class GameBackend {
    public GameState getGameState() {
        return gameState;
    }

    private GameState gameState;
    public List<Observer> observers;  // TODO: maybe only need one - the UI

    public GameBackend() {
        gameState = new GameState();
        observers = new ArrayList<>();
    }

    public void attach(Observer observer) {
        observers.add(observer);
    }

    private void notifyUpdate() {
        for (Observer o: observers) {
            o.update();
        }
    }

    public void newGame() {
        gameState = new GameState();
    }

    public void load(GameState gameState) {
        gameState = gameState;
    }

    public void shootArrow() {
        gameState.arrow = 200;
        notifyUpdate();
        while (gameState.arrow > 0) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameState.arrow -= 1;
            notifyUpdate();
        }
    }
}
