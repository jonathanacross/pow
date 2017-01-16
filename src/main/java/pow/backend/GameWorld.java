package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.gen.GenOverworldTopology;
import pow.backend.dungeon.gen.MapGenerator;
import pow.util.Array2D;
import pow.util.Point;
import pow.util.direction.Direction;
import pow.util.direction.DirectionSets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameWorld implements Serializable {
    public Map<String, GameMap> world;
    public GameMap currentMap; // the area where the player currently is

    public GameWorld(Random rng, Player player, Pet pet) {
        GenTestWorld(rng, player, pet);
    }

//    private void GenSimpleWorld(Random rng, Player player, Pet pet) {
//        world = new HashMap<>();
//        world.put("testArea", new GameMap(rng, player, pet));
//        currentMap  = world.get("testArea");
//    }

    private void GenTestWorld(Random rng, Player player, Pet pet) {

        // area 1.
        Map<String, String> area1Exits = new HashMap<>();
        area1Exits.put("east", "area2@west");
        area1Exits.put("south", "area3@north");
        MapGenerator.MapStyle area1Style = new MapGenerator.MapStyle("rock", "grass");
        GameMap area1 = MapGenerator.genMap(10, 10, area1Style, area1Exits, rng);

        // area 2.
        Map<String, String> area2Exits = new HashMap<>();
        area2Exits.put("west", "area1@east");
        MapGenerator.MapStyle area2Style = new MapGenerator.MapStyle("rock", "dark sand");
        GameMap area2 = MapGenerator.genMap(10, 20, area2Style, area2Exits, rng);

        // area 3.
        Map<String, String> area3Exits = new HashMap<>();
        area3Exits.put("north", "area1@south");
        MapGenerator.MapStyle area3Style = new MapGenerator.MapStyle("rock", "swamp");
        GameMap area3 = MapGenerator.genMap(20, 10, area3Style, area3Exits, rng);

        world = new HashMap<>();
        world.put("area1", area1);
        world.put("area2", area2);
        world.put("area3", area3);

        currentMap = area1;
        Point playerLoc = area1.findRandomOpenSquare(rng);
        area1.placePlayerAndPet(player, playerLoc, pet);

        // debug
        GenOverworldTopology topologyGenerator = new GenOverworldTopology(rng);
        System.out.println(topologyGenerator);
    }

}
