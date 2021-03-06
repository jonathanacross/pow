package pow.backend.dungeon.gen.worldgen;

import pow.util.Point3D;

import java.io.Serializable;
import java.util.*;

// Similar to MapTopology class, but holds just the summary data
// (e.g., can't be used to generate new worlds).  Used for displaying
// world map views.
public class MapTopologySummary implements Serializable {

    private final Map<Point3D, String> roomLocs;
    private final Set<SpacialConnection> connections;
    private final Map<String, MapPoint.PortalStatus> portals;

    public Map<Point3D, String> getRoomLocs() { return roomLocs; }
    public Set<SpacialConnection> getConnections() { return connections; }
    public Map<String, MapPoint.PortalStatus> getPortals() { return portals; }

    public MapTopologySummary(MapTopology mapTopology) {
        roomLocs = new HashMap<>();
        for (Map.Entry<Point3D, MapPoint> entry : mapTopology.getRoomLocs().entrySet()) {
            roomLocs.put(entry.getKey(), entry.getValue().id);
        }

        connections = mapTopology.getConnections();

        portals = mapTopology.getPortals();
    }
}
