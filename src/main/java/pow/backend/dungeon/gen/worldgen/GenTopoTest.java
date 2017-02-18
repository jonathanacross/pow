package pow.backend.dungeon.gen.worldgen;

import pow.util.Direction;
import pow.util.Point;
import pow.util.Point3D;

import java.io.IOException;
import java.util.*;

// generates the topology of rooms/levels in the outside/overworld
public class GenTopoTest {

//
//
//    public static void tryBuildMap(List<MapGenData> roomLinkDatas, Random rng) {
//        Map<Point3D, MapGenData> roomLocs = new HashMap<>();
//        List<MapConnection> connections = new ArrayList<>();
//
//        roomLocs.put(new Point3D(0,0,0), roomLinkDatas.get(0));
//
//        for (int i = 1; i < roomLinkDatas.size(); i++) {
//            MapConnection connection = findConnection(rng, roomLocs, roomLinkDatas.get(i), 100);
//            if (connection == null) {
//                System.out.println("failed to add room " + roomLinkDatas.get(i).id);
//                // TODO: wrap tryBuildMap in another method to retry
//                return;
//            }
//
//            Point3D newLoc = connection.fromLoc.plus(connection.dir);
//            roomLocs.put(newLoc, roomLinkDatas.get(i));
//            connections.add(connection);
//            connections.add(new MapConnection(newLoc, connection.dir.opposite));
//        }
//
//        Set<MapConnection> allConns = addAdditionalConnections(roomLocs, connections, rng, 0.25);
//
//        String view = toString(roomLocs, allConns);
//        System.out.println(view);
//    }
//
//    // Connect some percentage of adjacent rooms, even if they weren't
//    // connected originally. This will make it so the map will be more
//    // interesting than just be a big tree. Note that we only connect
//    // rooms that are on the same z-plane.
//    public static Set<MapConnection> addAdditionalConnections(
//            Map<Point3D, MapGenData> roomLocs,
//            List<MapConnection> connections, Random rng, double percentConnect)  {
//
//        List<Point3D> locs = new ArrayList<>();
//        locs.addAll(roomLocs.keySet());
//
//        Set<MapConnection> allConnections = new HashSet<>();
//        allConnections.addAll(connections);
//
//        for (int i = 0; i < locs.size(); i++) {
//            for (int j = i+1; j < locs.size(); j++) {
//                Point3D locI = locs.get(i);
//                Point3D locJ = locs.get(j);
//                int dist2 = locI.distSquared(locJ);
//                if (dist2 == 1 && rng.nextDouble() <= percentConnect && locI.z == locJ.z) {
//                    Point locI2D = new Point(locI.x, locI.y);
//                    Point locJ2D = new Point(locJ.x, locJ.y);
//                    Direction dir = Direction.getDir(locI2D, locJ2D);
//                    allConnections.add(new MapConnection(locI, dir));
//                    allConnections.add(new MapConnection(locJ, dir.opposite));
//                }
//            }
//        }
//
//        return allConnections;
//    }
//
//    private static List<Point3D> findConnectingLocations(Map<Point3D, MapGenData> roomLocs, MapGenData roomLinkData) {
//        List<Point3D> connectLocs = new ArrayList<>();
//        boolean useIds = !roomLinkData.fromIds.isEmpty();
//
//        for (Map.Entry<Point3D, MapGenData> entry : roomLocs.entrySet()) {
//            if ((useIds && roomLinkData.fromIds.contains(entry.getValue().id)) ||
//                (!useIds && roomLinkData.fromGroups.contains(entry.getValue().group))) {
//                connectLocs.add(entry.getKey());
//            }
//        }
//        if (connectLocs.isEmpty()) {
//            throw new RuntimeException("error: could not find any connections to room " + roomLinkData.id);
//        }
//        return connectLocs;
//    }
//
//    private static MapConnection findConnection(Random rng, Map<Point3D, MapGenData> roomLocs,
//                                                MapGenData room, int maxAttempts) {
//        int attempts = 0;
//
//        Point3D fromLoc;
//        Direction dir;
//        Point3D toLoc;
//        List<Point3D> connectingLocs = findConnectingLocations(roomLocs, room);
//        do {
//            fromLoc = connectingLocs.get(rng.nextInt(connectingLocs.size()));
//            dir = room.fromDirs.get(rng.nextInt(room.fromDirs.size()));
//            toLoc = fromLoc.plus(dir);
//            attempts++;
//        } while (attempts < maxAttempts && roomLocs.containsKey(toLoc));
//
//        if (attempts >= maxAttempts) {
//            return null;
//        } else {
//            return new MapConnection(fromLoc, dir);
//        }
//    }
//
//    public static String abbreviateId(String id) {
//        return id.substring(0,2) + id.charAt(id.length()-1);
//    }
//
//    // Makes a string to show the topology; useful for debugging, primarily.
//    // Only shows the rooms where z = 0, since 3d is hard.
////    @Override
//    public static String toString(
//            Map<Point3D, MapGenData> roomLocs,
//            Set<MapConnection> connections) {
//
//        // Find the bounds of what to print.
//        int xmin = Integer.MAX_VALUE;
//        int xmax = Integer.MIN_VALUE;
//        int ymin = Integer.MAX_VALUE;
//        int ymax = Integer.MIN_VALUE;
//        for (Point3D p : roomLocs.keySet()) {
//            if (p.x < xmin) xmin = p.x;
//            if (p.x > xmax) xmax = p.x;
//            if (p.y < ymin) ymin = p.y;
//            if (p.y > ymax) ymax = p.y;
//        }
//
//        int mapWidth = xmax - xmin + 1;
//        int mapHeight = ymax - ymin + 1;
//
//        // make empty area
//        int displayWidth = 4*mapWidth;
//        int displayHeight = 2*mapHeight;
//        char[][] displayArea = new char[displayWidth][displayHeight];
//        for (int x = 0; x < displayWidth; x++) {
//            for (int y = 0; y < displayHeight; y++) {
//                displayArea[x][y] = ' ';
//            }
//        }
//
//        // draw rooms
//        for (Map.Entry<Point3D, MapGenData> entry : roomLocs.entrySet()) {
//            Point3D p = entry.getKey();
//            int x = p.x - xmin;
//            int y = p.y - ymin;
//
//            if (p.z < 0) {
//                displayArea[4 * x + 2][2 * y + 1] = '>';
//            } else if (p.z > 0) {
//                displayArea[4 * x][2 * y + 1] = '<';
//            } else {
//                String abbrev = abbreviateId(entry.getValue().id);
//                for (int i = 0; i < abbrev.length(); i++) {
//                    displayArea[4 * x + i][2 * y] = abbrev.charAt(i);
//                }
//            }
//        }
//
//        // draw connections
//        for (MapConnection mapConnection : connections) {
//            Point3D p = mapConnection.fromLoc;
//            if (p.z != 0) continue;
//            int x = p.x - xmin;
//            int y = p.y - ymin;
//            int dx;
//            int dy;
//            char c;
//            if (mapConnection.dir == Direction.D || mapConnection.dir == Direction.U) continue;
//            switch (mapConnection.dir) {
//                case N: c = '|'; dx = 4 * x + 1; dy = 2 * y - 1; break;
//                case S: c = '|'; dx = 4 * x + 1; dy = 2 * y + 1; break;
//                case E: c = '-'; dx = 4 * x + 3; dy = 2 * y; break;
//                case W: c = '-'; dx = 4 * x - 1; dy = 2 * y; break;
//                default: continue;
//            }
//            displayArea[dx][dy] = c;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (int y = 0; y < displayHeight; y++) {
//            for (int x = 0; x < displayWidth; x++) {
//                sb.append(displayArea[x][y]);
//            }
//            sb.append('\n');
//        }
//        return sb.toString();
//    }
//
//
//    private int numGroups;
//    private int roomsPerGroup;
//    private double probMakeCycle;
//
//    private int roomGridSize;
//    private MapConnection[][] connections;
//    private List<MapConnection> rooms;
//    private List<List<MapConnection>> roomsInGroup;
//    private int currLevel;
//
//
//    public List<MapConnection> getMaps() {
//        return rooms;
//    }

    public static void main(String[] args) throws IOException {
        List<MapGenData> data = MapGenData.readLinkData();
        Random rng = new Random();
        MapTopology topology = new MapTopology(data, rng, 0.25);
    }
}
