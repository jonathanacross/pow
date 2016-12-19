package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.util.MessageLog;

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


    public GameState(String name) {
        this.rng = new Random(123);
        this.player = new Player("player", name, "human_adventurer", "yourself", -1, -1);
        this.pet = new Pet("pet", "your pet", "bot", "your pet", -1, -1);
        this.map = new GameMap(rng, player, pet); // fixes positions of player and pet
        this.log = new MessageLog(50);
    }
}
