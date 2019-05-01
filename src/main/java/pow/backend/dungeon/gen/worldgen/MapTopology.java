package pow.backend.dungeon.gen.worldgen;

import pow.util.Direction;
import pow.util.Point;
import pow.util.Point3D;

import java.util.*;

public class MapTopology {

    private Map<Point3D, MapPoint> roomLocs;
    private Set<SpacialConnection> connections;
    private final String firstMapId;
    private Map<String, MapPoint.PortalStatus> portals;

    public Map<Point3D, MapPoint> getRoomLocs() { return roomLocs; }
    public Set<SpacialConnection> getConnections() { return connections; }
    public String getFirstMapId() { return firstMapId; }
    public  Map<String, MapPoint.PortalStatus> getPortals() { return portals; }

    public MapTopology(List<MapPoint> mapPointList, Random rng, double probConnect) {
        firstMapId = mapPointList.get(0).id;
        int attempts = 0;
        while (!tryBuildMapTopology(mapPointList, rng, probConnect)) {
            attempts++;
        }
        if (attempts > 100) {
            throw new RuntimeException("couldn't create world topology after 100 attempts. failing.");
        }
    }

    // Makes a string to show the topology; useful for debugging, primarily.
    // Only shows the rooms where z = 0, since 3d is hard.
    @Override
    public String toString() {

        // Find the bounds of what to print.
        int xmin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymin = Integer.MAX_VALUE;
        int ymax = Integer.MIN_VALUE;
        for (Point3D p : roomLocs.keySet()) {
            if (p.x < xmin) xmin = p.x;
            if (p.x > xmax) xmax = p.x;
            if (p.y < ymin) ymin = p.y;
            if (p.y > ymax) ymax = p.y;
        }

        int mapWidth = xmax - xmin + 1;
        int mapHeight = ymax - ymin + 1;

        // make empty area
        int displayWidth = 4 * mapWidth;
        int displayHeight = 2 * mapHeight;
        char[][] displayArea = new char[displayWidth][displayHeight];
        for (int x = 0; x < displayWidth; x++) {
            for (int y = 0; y < displayHeight; y++) {
                displayArea[x][y] = ' ';
            }
        }

        // draw rooms
        for (Map.Entry<Point3D, MapPoint> entry : roomLocs.entrySet()) {
            Point3D p = entry.getKey();
            int x = p.x - xmin;
            int y = p.y - ymin;

            if (p.z < 0) {
                displayArea[4 * x + 2][2 * y + 1] = '>';
            } else if (p.z > 0) {
                displayArea[4 * x][2 * y + 1] = '<';
            } else {
                String abbrev = abbreviateId(entry.getValue().id);
                for (int i = 0; i < abbrev.length(); i++) {
                    displayArea[4 * x + i][2 * y] = abbrev.charAt(i);
                }
            }
        }

        // draw connections
        for (SpacialConnection spacialConnection : connections) {
            Point3D p = spacialConnection.fromLoc;
            if (p.z != 0) continue;
            int x = p.x - xmin;
            int y = p.y - ymin;
            int dx;
            int dy;
            char c;
            if (spacialConnection.dir == Direction.D || spacialConnection.dir == Direction.U) continue;
            switch (spacialConnection.dir) {
                case N: c = '|'; dx = 4 * x + 1; dy = 2 * y - 1; break;
                case S: c = '|'; dx = 4 * x + 1; dy = 2 * y + 1; break;
                case E: c = '-'; dx = 4 * x + 3; dy = 2 * y; break;
                case W: c = '-'; dx = 4 * x - 1; dy = 2 * y; break;
                default: continue;
            }
            displayArea[dx][dy] = c;
        }

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < displayHeight; y++) {
            for (int x = 0; x < displayWidth; x++) {
                sb.append(displayArea[x][y]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }



    // -------------- private implementation -------------

    // Attempts to build the topology between maps; this fills in roomLocs and connections.
    // Returns true if success.
    private boolean tryBuildMapTopology(List<MapPoint> mapPointList, Random rng, double probConnect) {
        this.roomLocs = new HashMap<>();
        List<SpacialConnection> baseConnections = new ArrayList<>();

        roomLocs.put(new Point3D(0, 0, 0), mapPointList.get(0));

        for (int i = 1; i < mapPointList.size(); i++) {
            SpacialConnection connection = findConnection(rng, roomLocs, mapPointList.get(i));
            if (connection == null) {
                return false;
            }

            Point3D newLoc = connection.fromLoc.plus(connection.dir);
            roomLocs.put(newLoc, mapPointList.get(i));
            baseConnections.add(connection);
            baseConnections.add(new SpacialConnection(newLoc, connection.dir.opposite));
        }

        this.connections = extendConnections(roomLocs, baseConnections, rng, probConnect);
        this.portals = extractPortals(mapPointList);
        return true;
    }

    // Connect some percentage of adjacent rooms, even if they weren't 
    // connected originally. This will make it so the map will be more 
    // interesting than just be a big tree. Note that we only connect 
    // rooms that are on the same z-plane.
    private static Set<SpacialConnection> extendConnections(
            Map<Point3D, MapPoint> roomLocs,
            List<SpacialConnection> connections, Random rng, double probConnect) {

        List<Point3D> locs = new ArrayList<>(roomLocs.keySet());

        Set<SpacialConnection> allConnections = new HashSet<>(connections);

        for (int i = 0; i < locs.size(); i++) {
            for (int j = i + 1; j < locs.size(); j++) {
                Point3D locI = locs.get(i);
                Point3D locJ = locs.get(j);
                int dist2 = locI.distSquared(locJ);
                if (dist2 == 1 && rng.nextDouble() <= probConnect && locI.z == locJ.z && locI.z == 0) {
                    Point locI2D = new Point(locI.x, locI.y);
                    Point locJ2D = new Point(locJ.x, locJ.y);
                    Direction dir = Direction.getDir(locI2D, locJ2D);
                    allConnections.add(new SpacialConnection(locI, dir));
                    allConnections.add(new SpacialConnection(locJ, dir.opposite));
                }
            }
        }

        return allConnections;
    }

    private static Map<String, MapPoint.PortalStatus> extractPortals(List<MapPoint> mapPointList) {
        Map<String, MapPoint.PortalStatus> portals = new HashMap<>();
        for (MapPoint mp : mapPointList) {
            portals.put(mp.id, mp.portalStatus);
        }
        return portals;
    }

    private static List<Point3D> findConnectingLocations(Map<Point3D, MapPoint> roomLocs, MapPoint roomLinkData) {
        List<Point3D> connectLocs = new ArrayList<>();
        boolean useIds = !roomLinkData.fromIds.isEmpty();

        for (Map.Entry<Point3D, MapPoint> entry : roomLocs.entrySet()) {
            if ((useIds && roomLinkData.fromIds.contains(entry.getValue().id)) ||
                    (!useIds && roomLinkData.fromGroups.contains(entry.getValue().group))) {
                connectLocs.add(entry.getKey());
            }
        }
        if (connectLocs.isEmpty()) {
            throw new RuntimeException("error: could not find any connections to room " + roomLinkData.id);
        }
        return connectLocs;
    }

    private static SpacialConnection findConnection(Random rng,
                                                    Map<Point3D, MapPoint> roomLocs,
                                                    MapPoint room) {
        int attempts = 0;
        final int maxAttempts = 100;

        Point3D fromLoc;
        Direction dir;
        Point3D toLoc;
        List<Point3D> connectingLocs = findConnectingLocations(roomLocs, room);
        do {
            fromLoc = connectingLocs.get(rng.nextInt(connectingLocs.size()));
            dir = room.fromDirs.get(rng.nextInt(room.fromDirs.size()));
            toLoc = fromLoc.plus(dir);
            attempts++;
        } while (attempts < maxAttempts && roomLocs.containsKey(toLoc));

        if (attempts >= maxAttempts) {
            return null;
        } else {
            return new SpacialConnection(fromLoc, dir);
        }
    }

    private static String abbreviateId(String id) {
        return id.substring(0, 2) + id.charAt(id.length() - 1);
    }
}
