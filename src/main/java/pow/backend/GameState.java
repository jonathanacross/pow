package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonObject;
import pow.util.DieRoll;
import pow.util.Point;

import java.io.Serializable;
import java.util.Collections;
import java.util.Random;

// class that just holds the data for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public final GameWorld world;
    public final Random rng;

    // character data
    public final Player player;
    public Pet pet;

    public int turnCount;

    // logging
    public final MessageLog log;

    public boolean gameInProgress;

    // convenience method
    public GameMap getCurrentMap() {
        return world.recentMaps.getCurrentMap();
    }

    // makes a partial GameState useful when not playing the actual game..
    public GameState() {
        this.world = null;
        this.rng = new Random();
        this.gameInProgress = false;
        this.player = new Player();
        this.pet = null;
        this.log = new MessageLog(GameConstants.MESSAGE_LOG_SIZE);
        this.turnCount = 0;
    }

    public GameState(String name) {
        this.gameInProgress = false;
        //int seed =  -524622737;
        int seed = (new Random()).nextInt();
        System.out.println("starting seed = " + seed);
        this.rng = new Random(seed);
        this.player = new Player();
        this.player.name = name;
        this.pet = new Pet(
                new DungeonObject.Params(
                        "pet", // id
                        "your pet", // name
                        "bot", // image
                        "your pet", // description
                        new Point(-1, -1), // location -- will be updated
                        true), // solid
                new Actor.Params(
                        1,
                        20, // maxHealth
                        3, // defense
                        0, // experience
                        new AttackData(new DieRoll(1, 4), 4, 0),
                        true, // friendly to player
                        false,
                        false,
                        0,
                        null,
                        0,
                        Collections.emptyList())
        );
        this.world = new GameWorld(rng, player, pet); // fixes positions of player and pet
        this.log = new MessageLog(GameConstants.MESSAGE_LOG_SIZE);
        log.add("Welcome to Pearls of Wisdom!");
        log.add("Press ? for help.");
        this.turnCount = 0;
    }
}
