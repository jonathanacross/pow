package pow.backend;

import pow.backend.dungeon.Player;
import pow.util.MessageLog;

import java.io.Serializable;
import java.util.Random;

// class that just holds the DATA for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public GameMap map;
    public Random rng;

    // character attributes
    public Player player;

    // logging
    public MessageLog log;

    public GameState(String name) {
        rng = new Random(123);
        map = new GameMap(rng);
        int x = map.width / 2;
        int y = map.height / 2;
        this.player = new Player("player", name, "human_adventurer", "yourself", x, y);
        this.log = new MessageLog(50);
    }
}
