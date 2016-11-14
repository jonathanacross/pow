package pow.backend;

import java.io.Serializable;

// class that just holds the DATA for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public GameMap map;

    // character attributes
    public int x;
    public int y;
    public String name;

    public GameState(String name) {
        map = new GameMap();
        x = 15;
        y = 15;
        this.name = name;
    }
}
