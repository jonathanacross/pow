package pow.backend;

import pow.util.MessageLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// class that just holds the DATA for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public GameMap map;

    // character attributes
    public int x;
    public int y;
    public String name;

    // logging
    public MessageLog log;

    public GameState(String name) {
        map = new GameMap();
        x = 15;
        y = 15;
        this.name = name;
        this.log = new MessageLog(50);
    }
}
