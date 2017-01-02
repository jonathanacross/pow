package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonObject;
import pow.util.Point;

import java.io.Serializable;
import java.util.Random;

// class that just holds the data for the game state
// so we can save/load properly
public class GameState implements Serializable {

    public GameMap map;
    public Random rng;

    // character attributes
    public Player player;
    public Pet pet;

    // logging
    public MessageLog log;

    public boolean gameInProgress;


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
                new Actor.Params(
                        30, // maxHealth
                        5, // dexterity
                        7, // defense
                        true, // friendly to player
                        0) // speed
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
                        true, // friendly to player
                        0) // speed
        );
        this.map = new GameMap(rng, player, pet); // fixes positions of player and pet
        this.log = new MessageLog(50);
    }
}
