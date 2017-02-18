package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.RecentMaps;
import pow.backend.dungeon.gen.mapgen.TestArea;
import pow.backend.dungeon.gen.worldgen.GenOverworldTopology;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.mapgen.MapGenerator;
import pow.backend.dungeon.gen.mapgen.RecursiveInterpolation;
import pow.backend.dungeon.gen.worldgen.MapGenData;
import pow.backend.dungeon.gen.worldgen.MapTopology;
import pow.util.DebugLogger;
import pow.util.Point;
import pow.util.Direction;
import pow.util.Point3D;

import java.io.Serializable;
import java.util.*;

public class GameWorld implements Serializable {
    public Map<String, GameMap> world;
    public RecentMaps recentMaps;

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
        area1Connections.add(new MapConnection("east", Direction.E, "area2", "west"));
        area1Connections.add(new MapConnection("south", Direction.S, "area3", "north"));
        RecursiveInterpolation.MapStyle area1Style = new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("grass", "bush", "big tree")),
                monsters, STAIRS_UP, STAIRS_DOWN);
        MapGenerator area1Gen = new RecursiveInterpolation(10, 0, area1Style, 1);
        GameMap area1 = area1Gen.genMap("area 1", area1Connections, rng);

        // area 2.
        List<MapConnection> area2Connections = new ArrayList<>();
        area1Connections.add(new MapConnection("west", Direction.W, "area1", "east"));
        RecursiveInterpolation.MapStyle area2Style = new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("dark sand", "cactus", "light pebbles")),
                monsters, STAIRS_UP, STAIRS_DOWN);
        MapGenerator area2Gen = new RecursiveInterpolation(10, 0, area2Style, 11);
        GameMap area2 = area2Gen.genMap("area 2", area2Connections, rng);

        // area 3.
        List<MapConnection> area3Connections = new ArrayList<>();
        area1Connections.add(new MapConnection("north", Direction.N, "area1", "south"));
        RecursiveInterpolation.MapStyle area3Style = new RecursiveInterpolation.MapStyle(
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("rock", null, null)),
                Arrays.asList(new RecursiveInterpolation.TerrainFeatureTriplet("swamp", "poison flower", "sick big tree")),
                monsters, STAIRS_UP, STAIRS_DOWN);
        MapGenerator area3Gen = new RecursiveInterpolation(10, 0, area3Style, 31);
        GameMap area3 = area3Gen.genMap("area 3", area3Connections, rng);

        world = new HashMap<>();
        world.put("area1", area1);
        world.put("area2", area2);
        world.put("area3", area3);

        recentMaps = new RecentMaps();
        recentMaps.setMap(area1);
        Point playerLoc = area1.findRandomOpenSquare(rng);
        area1.placePlayerAndPet(player, playerLoc, pet);
    }

//    private static final String AREA_NAME = "area";

    public List<MapConnection> getConnections(MapTopology topology, Point3D fromLoc) {
        List<MapConnection> namedConnections = new ArrayList<>();
        for (pow.backend.dungeon.gen.worldgen.MapConnection connection : topology.getConnections()) {
            if (connection.fromLoc.equals(fromLoc)) {
                Point3D toLoc = connection.fromLoc.plus(connection.dir);
                String destAreaId = topology.getRoomLocs().get(toLoc).id;
                String locName = connection.dir.name();
                String destLocName = connection.dir.opposite.name();
                namedConnections.add( new MapConnection(locName, connection.dir, destAreaId, destLocName) );
            }
        }
        return namedConnections;
    }


//    private List<MapConnection> getConnections(GenOverworldTopology.RoomConnection roomConnection) {
//        List<MapConnection> connections = new ArrayList<>();
//        for (Direction dir: roomConnection.dirToRoomIdx.keySet()) {
//            int adjId = roomConnection.dirToRoomIdx.get(dir);
//            String destAreaId = AREA_NAME + adjId;
//            String locName = dir.name();
//            String destLocName = dir.opposite.name();
//            connections.add( new MapConnection(locName, dir, destAreaId, destLocName) );
//        }
//
//        return connections;
//    }

    private void genMapWorld(Random rng, Player player, Pet pet) {

        // 1. generate overall structure of the world
        MapTopology topology;
        // TODO: put this in private constructor or instance of MapGenData
        try {
            List<MapGenData> data = MapGenData.readLinkData();
            topology = new MapTopology(data, rng, 0.25);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2. generate each area
        world = new HashMap<>();
        for (Map.Entry<Point3D, MapGenData> entry : topology.getRoomLocs().entrySet()) {
            MapGenerator mapGenerator = entry.getValue().mapGenerator;
            String id = entry.getValue().id;
            List<MapConnection> connections = getConnections(topology, entry.getKey());

            GameMap area = mapGenerator.genMap(id, connections, rng);
            world.put(id, area);
        }

        // 3. set up the player at the start
        GameMap startArea = world.get(topology.getFirstMapId());
        recentMaps = new RecentMaps();
        recentMaps.setMap(startArea);
        Point playerLoc = startArea.findRandomOpenSquare(rng);
        startArea.placePlayerAndPet(player, playerLoc, pet);
    }
}
