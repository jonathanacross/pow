package game;

import game.backend.GameMap;

import java.io.Serializable;

// class that just holds the DATA for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public enum MetaGameState {
        WELCOME,
        IN_GAME,
        WON,
        LOST
    }

    public MetaGameState metaGameState;
    public GameMap map;
    public int x;
    public int y;
    public int arrow;

    public GameState() {
        metaGameState = MetaGameState.WELCOME;
        map = new GameMap();
        x = 15;
        y = 15;
        arrow = -1;
    }
}
