package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.RecentMaps;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.mapgen.MapGenerator;
import pow.backend.dungeon.gen.worldgen.MapGenData;
import pow.backend.dungeon.gen.worldgen.MapTopology;
import pow.util.Point;
import pow.util.Point3D;

import java.io.Serializable;
import java.util.*;

public class GameWorld implements Serializable {
    public Map<String, GameMap> world;
    public RecentMaps recentMaps;

    public GameWorld(Random rng, Player player, Pet pet) {
        genMapWorld(rng, player, pet);
    }

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
