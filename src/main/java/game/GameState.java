package game;

import game.backend.GameMap;

import java.io.Serializable;

// class that just holds the DATA for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public GameMap map;
    public int x;
    public int y;
    public int arrow;

    public GameState() {
        map = new GameMap();
        x = 15;
        y = 15;
        arrow = -1;
    }
}
