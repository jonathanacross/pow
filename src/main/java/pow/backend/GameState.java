package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonObject;
import pow.util.DieRoll;
import pow.util.Point;

import java.io.Serializable;
import java.util.Random;

// class that just holds the data for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public GameWorld world;
    public Random rng;

    // character attributes
    public Player player;
    public Pet pet;

    // logging
    public MessageLog log;

    public boolean gameInProgress;

    // makes a partial gamestate useful when not playing the actual game..
    public GameState() {
        this.world = null;
        this.rng = new Random(123);
        this.gameInProgress = false;
        this.player = new Player(
                new DungeonObject.Params(
                        "player", // id
                        "", // name
                        "human_adventurer", // image
                        "yourself", // description
                        new Point(-1, -1), // location -- will be updated later
                        true), // solid
                30, // maxHealth
                1, // cStr
                1, // cDex
                1, // cInt
                1, // cCon
                new DieRoll(2, 2, 0) // attack
        );
        this.pet = null;
        this.log = new MessageLog(50);
    }

    public GameState(String name) {
        this.gameInProgress = false;
        this.rng = new Random(123);
        this.player = new Player(
                new DungeonObject.Params(
                        "player", // id
                        name, // name
                        "human_adventurer", // image
                        "yourself", // description
                        new Point(-1, -1), // location -- will be updated
                        true), // solid
                30, // maxHealth
                1, // cStr
                1, // cDex
                1, // cInt
                1, // cCon
                new DieRoll(2, 2, 0) // attack
        );

        this.pet = new Pet(
                new DungeonObject.Params(
                        "pet", // id
                        "your pet", // name
                        "bot", // image
                        "your pet", // description
                        new Point(-1, -1), // location -- will be updated
                        true), // solid
                new Actor.Params(
                        20, // maxHealth
                        4, // dexterity
                        3, // defense
                        new DieRoll(1, 4, 0),
                        true, // friendly to player
                        0) // speed
        );
        this.world = new GameWorld(rng, player, pet); // fixes positions of player and pet
        this.log = new MessageLog(50);
        log.add("Welcome to Pearls of Wisdom!");
        log.add("Press ? for help.");
    }
}
