package game;

import java.io.Serializable;

// class that just holds the DATA for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public int x;
    public int y;
    public int arrow;

    public GameState() {
        x = 50;
        y = 50;
        arrow = -1;
    }
}
