package pow.backend;

import pow.backend.actors.Player;
import pow.backend.dungeon.RecentMaps;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.mapgen.MapGenerator;
import pow.backend.dungeon.gen.worldgen.*;
import pow.util.Point;
import pow.util.Point3D;

import java.io.Serializable;
import java.util.*;

public class GameWorld implements Serializable {
    public Map<String, GameMap> world;
    public RecentMaps recentMaps;
    public MapTopologySummary topologySummary;

    public GameWorld(Random rng, Player player, Player pet) {
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

    private void genMapWorld(Random rng, Player player, Player pet) {
        // 1. generate overall structure of the world, and save a summary copy
        List<MapPoint> data = WorldDataGen.getMapPoints(GameConstants.USE_TEST_WORLD);
        MapTopology topology = new MapTopology(data, rng, GameConstants.PROB_CONNECT_ADJ_AREAS);
        topologySummary = new MapTopologySummary(topology);

        // 2. generate each area
        world = new HashMap<>();
        for (Map.Entry<Point3D, MapPoint> entry : topology.getRoomLocs().entrySet()) {
            MapGenerator mapGenerator = entry.getValue().mapGenerator;
            String id = entry.getValue().id;
            List<MapConnection> connections = getConnections(topology, entry.getKey());
            MapPoint.PortalStatus portalStatus = topologySummary.getPortals().get(id);

            GameMap area = mapGenerator.genMap(id, connections, portalStatus, rng);
            world.put(id, area);
        }

        // 3. set up the player at the start
        GameMap startArea = world.get(topology.getFirstMapId());
        recentMaps = new RecentMaps();
        recentMaps.setMap(startArea);
        GeneratorUtils.regenMonstersForCurrentMap(startArea, rng);
        Point playerLoc = startArea.findRandomOpenSquare(rng);
        startArea.placePlayerAndPet(player, playerLoc, pet);
    }
}
