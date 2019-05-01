package pow.backend;

import pow.backend.actors.Player;

import java.io.Serializable;
import java.util.Random;

// class that just holds the data for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public final GameWorld world;
    public final Random rng;

    // character data
    public final Party party;

    public int turnCount;

    // logging
    public final MessageLog log;

    // convenience method
    public GameMap getCurrentMap() {
        return world.recentMaps.getCurrentMap();
    }

    public GameState(Player player) {
        int seed = (new Random()).nextInt();
        this.rng = new Random(seed);
        this.party = new Party(player);
        this.world = new GameWorld(rng, party); // fixes positions of player and pet
        this.log = new MessageLog(GameConstants.MESSAGE_LOG_SIZE);
        log.add("Welcome to Pearls of Wisdom!", MessageLog.MessageType.GAME_EVENT);
        log.add("Press ? for help.", MessageLog.MessageType.GENERAL);
        this.turnCount = 0;
    }
}
