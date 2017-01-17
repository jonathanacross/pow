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

        // area 1.
        Map<String, String> area1Exits = new HashMap<>();
        area1Exits.put("east", "area2@west");
        area1Exits.put("south", "area3@north");
        MapGenerator.MapStyle area1Style = new MapGenerator.MapStyle("rock", "grass");
        GameMap area1 = MapGenerator.genMap("area 1", 10, 10, area1Style, area1Exits, rng);

        // area 2.
        Map<String, String> area2Exits = new HashMap<>();
        area2Exits.put("west", "area1@east");
        MapGenerator.MapStyle area2Style = new MapGenerator.MapStyle("rock", "dark sand");
        GameMap area2 = MapGenerator.genMap("area 2", 10, 20, area2Style, area2Exits, rng);

        // area 3.
        Map<String, String> area3Exits = new HashMap<>();
        area3Exits.put("north", "area1@south");
        MapGenerator.MapStyle area3Style = new MapGenerator.MapStyle("rock", "swamp");
        GameMap area3 = MapGenerator.genMap("area 3", 20, 10, area3Style, area3Exits, rng);

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
            new MapGenerator.MapStyle("rock", "grass"),
            new MapGenerator.MapStyle("rock", "dark sand"),
            new MapGenerator.MapStyle("waves", "water 3"),
            new MapGenerator.MapStyle("snowy rock", "snow"),
            new MapGenerator.MapStyle("rock", "swamp"),
            new MapGenerator.MapStyle("rock", "cold lava floor"),
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

                GameMap area = MapGenerator.genMap("area " + levelIdx, 100, 100, style, exits, rng);
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
