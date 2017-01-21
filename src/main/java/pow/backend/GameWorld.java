package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.gen.worldgen.GenOverworldTopology;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.ProtoTranslator;
import pow.backend.dungeon.gen.mapgen.MapGenerator;
import pow.backend.dungeon.gen.mapgen.RecursiveInterpolation;
import pow.backend.dungeon.gen.mapgen.ShapeDLA;
import pow.util.DebugLogger;
import pow.util.Point;
import pow.util.direction.DirectionSets;

import java.io.Serializable;
import java.util.*;

public class GameWorld implements Serializable {
    public Map<String, GameMap> world;
    public GameMap currentMap; // the area where the player currently is

    public GameWorld(Random rng, Player player, Pet pet) {
        //genTestWorld(rng, player, pet);
        genMapWorld(rng, player, pet);
    }

    private static final String STAIRS_UP = "stairs up";
    private static final String STAIRS_DOWN = "stairs up";
    private static final String DUNGEON_ENTRANCE = "dungeon entrance";

    // small sample with 3 rooms
    private void genTestWorld(Random rng, Player player, Pet pet) {

        List<String> monsters = Arrays.asList("green mushrooms", "yellow ant", "scruffy dog");

        // area 1.
        List<MapConnection> area1Connections = new ArrayList<>();
        area1Connections.add(new MapConnection("east", MapConnection.Direction.E, "area2", "west"));
        area1Connections.add(new MapConnection("south", MapConnection.Direction.S, "area3", "north"));
        RecursiveInterpolation.MapStyle area1Style = new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("grass", "bush", "big tree")),
                monsters, STAIRS_UP, STAIRS_DOWN);
        MapGenerator area1Gen = new RecursiveInterpolation(10, 0, area1Style);
        GameMap area1 = area1Gen.genMap("area 1", area1Connections, rng);

        // area 2.
        List<MapConnection> area2Connections = new ArrayList<>();
        area1Connections.add(new MapConnection("west", MapConnection.Direction.W, "area1", "east"));
        RecursiveInterpolation.MapStyle area2Style = new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("dark sand", "cactus", "light pebbles")),
                monsters, STAIRS_UP, STAIRS_DOWN);
        MapGenerator area2Gen = new RecursiveInterpolation(10, 0, area2Style);
        GameMap area2 = area2Gen.genMap("area 2", area2Connections, rng);

        // area 3.
        List<MapConnection> area3Connections = new ArrayList<>();
        area1Connections.add(new MapConnection("north", MapConnection.Direction.N, "area1", "south"));
        RecursiveInterpolation.MapStyle area3Style = new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("swamp", "poison flower", "sick big tree")),
                monsters, STAIRS_UP, STAIRS_DOWN);
        MapGenerator area3Gen = new RecursiveInterpolation(10, 0, area3Style);
        GameMap area3 = area3Gen.genMap("area 3", area3Connections, rng);

        world = new HashMap<>();
        world.put("area1", area1);
        world.put("area2", area2);
        world.put("area3", area3);

        currentMap = area1;
        Point playerLoc = area1.findRandomOpenSquare(rng);
        area1.placePlayerAndPet(player, playerLoc, pet);
    }

    private static final String AREA_NAME = "area";

    private List<MapConnection> getConnections(GenOverworldTopology.RoomConnection roomConnection) {
        List<MapConnection> connections = new ArrayList<>();
        for (int d = 0; d < DirectionSets.Cardinal.size(); d++) {
            if (roomConnection.adjroomIdx[d] >= 0) {
                int adjId = roomConnection.adjroomIdx[d];
                String destAreaId = AREA_NAME + adjId;
                // TODO: see if it's worth refactoring RoomConnection to use MapConnection.Direction
                MapConnection.Direction dir = null;
                switch (d) {
                    case DirectionSets.Cardinal.N: dir = MapConnection.Direction.N; break;
                    case DirectionSets.Cardinal.E: dir = MapConnection.Direction.E; break;
                    case DirectionSets.Cardinal.S: dir = MapConnection.Direction.S; break;
                    case DirectionSets.Cardinal.W: dir = MapConnection.Direction.W; break;
                }
                String locName = dir.name();
                String destLocName = dir.opposite().name();
                connections.add( new MapConnection(locName, dir, destAreaId, destLocName) );
            }
        }

        return connections;
    }

    private void genMapWorld(Random rng, Player player, Pet pet) {
        int numGroups = 7;
        RecursiveInterpolation.MapStyle[] styles = {
            // grassy fields
            new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("grass", "bush", "big tree")),
                Arrays.asList("bit", "bot", "yellow ant", "pigeon", "yellow snake", "scruffy dog"),
                STAIRS_UP, DUNGEON_ENTRANCE),
            // desert
            new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("dark sand", "cactus", "light pebbles")),
                Arrays.asList("novice mage", "novice warrior", "novice archer", "novice rogue", "red ant", "cobra", "green centipede", "pincer beetle", "dust devil", "jackal", "brown scorpion"),
                STAIRS_UP, DUNGEON_ENTRANCE),
            // forest
            new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("forest", "big tree", "pine tree")),
                Arrays.asList("rabbit mage", "rabbit warrior", "rabbit archer", "rabbit rogue", "purple beetle", "blue snake", "black spider", "green imp", "small red spiny", "rock lizard", "yellow worm mass", "yellow schweinhund"),
                STAIRS_UP, DUNGEON_ENTRANCE),
            // water
            new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("waves", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("water 3", null, "water 4")),
                Arrays.asList("goldfish", "green fish", "eel", "pink jellyfish", "copper jellyfish", "scaryfish", "water whirlwind", "octopus", "medusa", "sea dragon"),
                STAIRS_UP, DUNGEON_ENTRANCE),
            // snow
            new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("snowy rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("snow", "snowy pine tree", "white small tree")),
                Arrays.asList("dark elf mage", "dark elf warrior", "dark elf archer", "dark elf rogue", "baby blue dragon", "baby yellow dragon", "blue beetle", "gray wolf", "bear", "white wolf", "big red spiny", "frost giant" ),
                STAIRS_UP, DUNGEON_ENTRANCE),
            // swamp
            new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("swamp", "poison flower", "sick big tree")),
                Arrays.asList("orc mage", "orc warrior", "orc archer", "orc rogue", "baby green dragon", "baby red dragon", "gold dragonfly", "purple worms", "golem", "griffin", "chess knight", "copperhead snake"),
                STAIRS_UP, DUNGEON_ENTRANCE),
            // volcano
            new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("cold lava floor", null, "dark pebbles")),
                Arrays.asList("demon mage", "demon warrior", "demon archer", "demon rogue", "green dragon", "red dragon", "creeping magma", "lava beetle", "mumak", "iron golem", "fire vortex", "lava dragon"),
                STAIRS_UP, DUNGEON_ENTRANCE),
        };

        int roomsPerGroup = 2;
        double probMakeCycle = 0.25;
        GenOverworldTopology topologyGenerator = new GenOverworldTopology(rng, numGroups, roomsPerGroup, probMakeCycle);
        List<GenOverworldTopology.RoomConnection> roomConnections = topologyGenerator.getRooms();
        DebugLogger.info(topologyGenerator.toString());

        // add a sample dungeon to test up/down stairs.
        // TODO: add a method that can act on a dungeon topology and add a connection to two areas.
        final String UP_LOC_NAME = "up";
        final String DOWN_LOC_NAME = "down dungeon1";
        final String TEST_DUNGEON_ID = "D1L1";

        List<MapConnection> dungeon1Connections = new ArrayList<>();
        dungeon1Connections.add(new MapConnection(UP_LOC_NAME, MapConnection.Direction.U, "area0", DOWN_LOC_NAME));
        ProtoTranslator dungeon1Style = new ProtoTranslator(1);
        List<String> dungeon1Monsters = Arrays.asList("yellow snake", "scruffy dog", "yellow mushrooms", "floating eye", "bat", "green worm mass", "brown imp");
        MapGenerator dungeon1Gen = new ShapeDLA(dungeon1Style, dungeon1Monsters, 50, 50);
        GameMap dungeon1 = dungeon1Gen.genMap(TEST_DUNGEON_ID, dungeon1Connections, rng);

        world = new HashMap<>();
        for (int group = 0; group < numGroups; group++) {
            MapGenerator mapGenerator = new RecursiveInterpolation(6, 3, styles[group]);
            for (int room = 0; room < roomsPerGroup; room++) {
                int levelIdx = group * roomsPerGroup + room;

                GenOverworldTopology.RoomConnection roomConnection = roomConnections.get(levelIdx);
                List<MapConnection> connections = getConnections(roomConnection);
                if (group == 0 && room == 0) {
                    connections.add(new MapConnection(DOWN_LOC_NAME, MapConnection.Direction.D, TEST_DUNGEON_ID, UP_LOC_NAME));
                }

                GameMap area = mapGenerator.genMap("area " + levelIdx, connections, rng);
                world.put(AREA_NAME + roomConnection.level, area);
            }
        }

        world.put(TEST_DUNGEON_ID, dungeon1);


        // set up the player at the start
        GameMap startArea = world.get(AREA_NAME + "0");
        currentMap = startArea;
        Point playerLoc = startArea.findRandomOpenSquare(rng);
        startArea.placePlayerAndPet(player, playerLoc, pet);
    }
}
