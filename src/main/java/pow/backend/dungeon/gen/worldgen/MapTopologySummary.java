package pow.backend.dungeon.gen.worldgen;

import pow.util.Point3D;

import java.io.Serializable;
import java.util.*;

// Similar to MapTopology class, but holds just the summary data
// (e.g., can't be used to generate new worlds).  Used for displaying
// world map views.
public class MapTopologySummary implements Serializable {

    private Map<Point3D, String> roomLocs;
    private Set<SpacialConnection> connections;

    public Map<Point3D, String> getRoomLocs() { return roomLocs; }
    public Set<SpacialConnection> getConnections() { return connections; }

    public MapTopologySummary(MapTopology mapTopology) {
        roomLocs = new HashMap<>();
        for (Map.Entry<Point3D, MapPoint> entry : mapTopology.getRoomLocs().entrySet()) {
            roomLocs.put(entry.getKey(), entry.getValue().id);
        }

        connections = mapTopology.getConnections();
    }
}
