package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameWorld implements Serializable {
    public Map<String, GameMap> world;
    public GameMap currentMap; // the area where the player currently is

    public GameWorld(Random rng, Player player, Pet pet) {
        world = new HashMap<>();
        world.put("testArea", new GameMap(rng, player, pet));
        currentMap  = world.get("testArea");
    }
}
