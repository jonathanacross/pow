package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.gen.GenOverworldTopology;
import pow.backend.dungeon.gen.MapGenerator;
import pow.util.Array2D;
import pow.util.DebugLogger;
import pow.util.Point;
import pow.util.direction.Direction;
import pow.util.direction.DirectionSets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameWorld implements Serializable {
    public Map<String, GameMap> world;
    public GameMap currentMap; // the area where the player currently is

    public GameWorld(Random rng, Player player, Pet pet) {
        //genTestWorld(rng, player, pet);
        genMapWorld(rng, player, pet);
    }

    // small sample with 3 rooms
    private void genTestWorld(Random rng, Player player, Pet pet) {

        List<String> monsters = Arrays.asList("green mushrooms", "yellow ant", "scruffy dog");

        // area 1.
        Map<String, String> area1Exits = new HashMap<>();
        area1Exits.put("east", "area2@west");
        area1Exits.put("south", "area3@north");
        MapGenerator.MapStyle area1Style = new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("grass", "bush", "green tree")),
                monsters);
        GameMap area1 = MapGenerator.genMap("area 1", 10, 10, 0, area1Style, area1Exits, rng);

        // area 2.
        Map<String, String> area2Exits = new HashMap<>();
        area2Exits.put("west", "area1@east");
        MapGenerator.MapStyle area2Style = new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("dark sand", "cactus", "light pebbles")),
                monsters);
        GameMap area2 = MapGenerator.genMap("area 2", 10, 20, 0, area2Style, area2Exits, rng);

        // area 3.
        Map<String, String> area3Exits = new HashMap<>();
        area3Exits.put("north", "area1@south");
        MapGenerator.MapStyle area3Style = new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("swamp", "swamp flower", "swamp tree")),
                monsters);
        GameMap area3 = MapGenerator.genMap("area 3", 20, 10, 0, area3Style, area3Exits, rng);

        world = new HashMap<>();
        world.put("area1", area1);
        world.put("area2", area2);
        world.put("area3", area3);

        currentMap = area1;
        Point playerLoc = area1.findRandomOpenSquare(rng);
        area1.placePlayerAndPet(player, playerLoc, pet);
    }

    private static final String AREA_NAME = "area";

    private Map<String, String> getExits(GenOverworldTopology.RoomConnection roomConnection) {
        Map<String, String> exits = new HashMap<>();
        for (int d = 0; d < DirectionSets.Cardinal.size(); d++) {
            int oppD = DirectionSets.Cardinal.getOpposite(d);
            if (roomConnection.adjroomIdx[d] >= 0) {
                int adjId = roomConnection.adjroomIdx[d];
                exits.put(DirectionSets.Cardinal.getName(d),
                        AREA_NAME + adjId + "@" + DirectionSets.Cardinal.getName(oppD));
            }
        }

        return exits;
    }

    private void genMapWorld(Random rng, Player player, Pet pet) {
        int numGroups = 6;
        MapGenerator.MapStyle[] styles = {
            // grassy fields
            new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("grass", "bush", "green tree")),
                Arrays.asList("bit", "bot", "yellow ant", "pigeon", "yellow snake", "scruffy dog", "yellow mushrooms", "floating eye", "bat", "green worm mass", "brown imp")),
            // desert
            new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("dark sand", "cactus", "light pebbles")),
                Arrays.asList("novice mage", "novice warrior", "novice archer", "novice rogue", "red ant", "cobra", "green centipede", "pincer beetle", "dust devil", "jackal", "brown scorpion")),
            // water
            new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("waves", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("water 3", null, "water 4")),
                Arrays.asList("goldfish", "green fish", "eel", "pink jellyfish", "copper jellyfish", "scaryfish", "water whirlwind", "octopus", "medusa", "sea dragon")),
            // snow
            new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("snowy rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("snow", "snowy pine tree", "small white tree")),
                Arrays.asList("dark elf mage", "dark elf warrior", "dark elf archer", "dark elf rogue", "baby blue dragon", "baby yellow dragon", "blue beetle", "gray wolf", "bear", "white wolf", "big red spiny", "frost giant" )),
            // swamp
            new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("swamp", "swamp flower", "swamp tree")),
                Arrays.asList("orc mage", "orc warrior", "orc archer", "orc rogue", "baby green dragon", "baby red dragon", "gold dragonfly", "purple worms", "golem", "griffin", "chess knight", "copperhead snake")),
            // volcano
            new MapGenerator.MapStyle(
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new MapGenerator.TerrainFeatureTriplet("cold lava floor", null, "dark pebbles")),
                Arrays.asList("demon mage", "demon warrior", "demon archer", "demon rogue", "green dragon", "red dragon", "creeping magma", "lava beetle", "mumak", "iron golem", "fire vortex", "lava dragon")),
        };

        int roomsPerGroup = 2;
        double probMakeCycle = 0.25;
        GenOverworldTopology topologyGenerator = new GenOverworldTopology(rng, numGroups, roomsPerGroup, probMakeCycle);
        List<GenOverworldTopology.RoomConnection> roomConnections = topologyGenerator.getRooms();
        DebugLogger.info(topologyGenerator.toString());

        world = new HashMap<>();
        for (int group = 0; group < numGroups; group++) {
            MapGenerator.MapStyle style = styles[group];
            for (int room = 0; room < roomsPerGroup; room++) {
                int levelIdx = group * roomsPerGroup + room;

                GenOverworldTopology.RoomConnection roomConnection = roomConnections.get(levelIdx);
                Map<String, String> exits = getExits(roomConnection);

                GameMap area = MapGenerator.genMap("area " + levelIdx, 5, 5, 3, style, exits, rng);
                world.put(AREA_NAME + roomConnection.level, area);
            }
        }

        // set up the player at the start
        GameMap startArea = world.get(AREA_NAME + "0");
        currentMap = startArea;
        Point playerLoc = startArea.findRandomOpenSquare(rng);
        startArea.placePlayerAndPet(player, playerLoc, pet);
    }

}
