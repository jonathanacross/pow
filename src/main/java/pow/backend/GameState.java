package pow.backend;

import pow.util.MessageLog;
import pow.util.Random;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// class that just holds the DATA for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public GameMap map;
    public Random rng;

    // character attributes
    public int x;
    public int y;
    public String name;

    // logging
    public MessageLog log;

    public GameState(String name) {
        rng = new Random(123);
        map = new GameMap(rng);
        x = map.width / 2;
        y = map.height / 2;
        this.name = name;
        this.log = new MessageLog(50);
    }
}
