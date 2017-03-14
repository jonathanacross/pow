package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.RecentMaps;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.mapgen.MapGenerator;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.backend.dungeon.gen.worldgen.MapTopology;
import pow.backend.dungeon.gen.worldgen.SpacialConnection;
import pow.backend.dungeon.gen.worldgen.WorldDataGen;
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

    private List<MapConnection> getConnections(MapTopology topology, Point3D fromLoc) {
        List<MapConnection> namedConnections = new ArrayList<>();
        for (SpacialConnection connection : topology.getConnections()) {
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
        List<MapPoint> data = WorldDataGen.getMapPoints();  // or, getTestMapPoints();
        MapTopology topology = new MapTopology(data, rng, 0.25);

        // 2. generate each area
        world = new HashMap<>();
        for (Map.Entry<Point3D, MapPoint> entry : topology.getRoomLocs().entrySet()) {
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
