package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.actors.ai.StepMovement;
import pow.backend.dungeon.DungeonObject;
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
    public Actor selectedActor;  // which actor is the player controlling?

    public int turnCount;

    // logging
    public final MessageLog log;

    public boolean gameInProgress;
    public boolean inPortal;  // Indicates that the player is in a portal (and needs to indicate where they will exit).

    // convenience method
    public GameMap getCurrentMap() {
        return world.recentMaps.getCurrentMap();
    }

    // makes a partial GameState useful when not playing the actual game..
    public GameState() {
        this.world = null;
        this.rng = new Random();
        this.gameInProgress = false;
        this.inPortal = false;
        this.player = new Player();
        this.selectedActor = this.player;
        this.pet = null;
        this.log = new MessageLog(GameConstants.MESSAGE_LOG_SIZE);
        this.turnCount = 0;
    }

    public GameState(Player player) {
        this.gameInProgress = false;
        //int seed =  -524622737;
        int seed = (new Random()).nextInt();
        System.out.println("starting seed = " + seed);
        this.rng = new Random(seed);
        this.player = player;
        this.selectedActor = this.player;
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
                        0,
                        true,
                        false,
                        false,
                        new StepMovement(),
                        null,
                        0,
                        12,
                        10,
                        8,
                        8,
                        0,
                        Collections.emptyList()), this);
        this.world = new GameWorld(rng, player, pet); // fixes positions of player and pet
        this.log = new MessageLog(GameConstants.MESSAGE_LOG_SIZE);
        log.add("Welcome to Pearls of Wisdom!", MessageLog.MessageType.GAME_EVENT);
        log.add("Press ? for help.", MessageLog.MessageType.GENERAL);
        this.turnCount = 0;
    }
}
