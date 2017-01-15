package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.gen.MapGenerator;
import pow.util.Array2D;
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
        GenTestWorld(rng, player, pet);
    }

//    private void GenSimpleWorld(Random rng, Player player, Pet pet) {
//        world = new HashMap<>();
//        world.put("testArea", new GameMap(rng, player, pet));
//        currentMap  = world.get("testArea");
//    }

    private void GenTestWorld(Random rng, Player player, Pet pet) {

        // area 1.
        Map<String, String> area1Exits = new HashMap<>();
        area1Exits.put("east", "area2@west");
        area1Exits.put("south", "area3@north");
        MapGenerator.MapStyle area1Style = new MapGenerator.MapStyle("rock", "grass");
        GameMap area1 = MapGenerator.genMap(10, 10, area1Style, area1Exits, rng);

        // area 2.
        Map<String, String> area2Exits = new HashMap<>();
        area2Exits.put("west", "area1@east");
        MapGenerator.MapStyle area2Style = new MapGenerator.MapStyle("rock", "dark sand");
        GameMap area2 = MapGenerator.genMap(10, 20, area2Style, area2Exits, rng);

        // area 3.
        Map<String, String> area3Exits = new HashMap<>();
        area3Exits.put("north", "area1@south");
        MapGenerator.MapStyle area3Style = new MapGenerator.MapStyle("rock", "swamp");
        GameMap area3 = MapGenerator.genMap(20, 10, area3Style, area3Exits, rng);

        world = new HashMap<>();
        world.put("area1", area1);
        world.put("area2", area2);
        world.put("area3", area3);

        currentMap = area1;
        Point playerLoc = area1.findRandomOpenSquare(rng);
        area1.placePlayerAndPet(player, playerLoc, pet);

        // debug
        genWorldTopology(rng);
    }

    private class RoomConnection {
        public int level;
        public boolean[] connect;
        public int x;
        public int y;

        public RoomConnection(int x, int y) {
            level = -1;
            connect = new boolean[DirectionSets.Cardinal.size()];
            this.x = x;
            this.y = y;
        }
    }

    private Point getRandomStartPoint(Random rng, RoomConnection[][] connections) {
        int roomGridSize = Array2D.width(connections);

        int x;
        int y;
        do {
            x = rng.nextInt(roomGridSize);
            y = rng.nextInt(roomGridSize);
        } while (connections[x][y] == null);

        return new Point(x,y);
    }

    private void perturbPoint(Random rng, Point p, int roomGridSize) {
        int dx = rng.nextInt(3) - 1;
        int dy = rng.nextInt(3) - 1;
        int mid = roomGridSize / 2;

        // if the shape is at the edge, force the direction to move
        if (p.x == 0) { dx = 1; }
        if (p.y == 0) { dy = 1; }
        if (p.x == roomGridSize - 1) { dx = -1; }
        if (p.y == roomGridSize - 1) { dy = -1; }

        // if dx or dy = 0, then move towards center (for fast convergence)
        if (dx == 0) { dx = p.x < mid ? 1 : -1; }
        if (dy == 0) { dy = p.y < mid ? 1 : -1; }

        p.shiftBy(new Point(dx, dy));
    }

    private List<RoomConnection> genWorldTopology(Random rng) {
        final int numGroups = 7;
        final int roomsPerGroup = 4;
        final double probMakeCycle = 0.3;

        final int numLevels = numGroups * roomsPerGroup;

        int roomGridSize = 3 * (int) Math.sqrt(numLevels);
        RoomConnection[][] connections = new RoomConnection[roomGridSize][roomGridSize];
        List<RoomConnection> rooms = new ArrayList<>();  // same data, but just the rooms of interest

        int level = 0;
        for (int group = 0; group < numGroups; group++) {

            List<RoomConnection> roomsInGroup = new ArrayList<>();

            for (int levelInGroup = 0; levelInGroup < roomsPerGroup; levelInGroup++) {
                // initial condition -- put a room in the center
                if (group == 0 && levelInGroup == 0) {
                    Point start = new Point(roomGridSize / 2, roomGridSize / 2);
                    RoomConnection startRoom = new RoomConnection(start.x, start.y);
                    startRoom.level = level;
                    level++;
                    connections[start.x][start.y] = startRoom;
                    rooms.add(startRoom);
                    roomsInGroup.add(startRoom);
                    continue;
                }

                // add new room to current group
                int x;
                int y;
                int dir;
                RoomConnection randRoom = null;
                do {
                    if (levelInGroup == 0)
                        randRoom = rooms.get(rng.nextInt(rooms.size()));
                    else
                        randRoom = roomsInGroup.get(rng.nextInt(roomsInGroup.size()));

                    // pick random direction from this room
                    dir = rng.nextInt(DirectionSets.Cardinal.size());
                    Direction d = DirectionSets.Cardinal.getDirection(dir);
                    x = randRoom.x + d.dx;
                    y = randRoom.y + d.dy;
                } while (!(x >= 0 && x < roomGridSize && y >= 0 && y < roomGridSize) || connections[x][y] != null);

                // found a valid space
                RoomConnection newRoom = new RoomConnection(x, y);
                newRoom.level = level;
                level++;
                connections[x][y] = newRoom;
                rooms.add(newRoom);
                roomsInGroup.add(newRoom);

                // connect room to adjacent rooms
                for (int dIdx = 0; dIdx < DirectionSets.Cardinal.size(); dIdx++) {
                    int dOppIdx = DirectionSets.Cardinal.getOpposite(dIdx);
                    double probConnect = (dir == dOppIdx) ? 1.0 : probMakeCycle;
                    Direction d = DirectionSets.Cardinal.getDirection(dIdx);
                    int nx = newRoom.x + d.dx;
                    int ny = newRoom.y + d.dy;
                    if (nx < 0 || ny < 0 || nx >= roomGridSize || ny >= roomGridSize) continue;
                    if (connections[nx][ny] == null) continue;
                    RoomConnection adjRoom = connections[nx][ny];
                    if (rng.nextDouble() <= probConnect) {
                        newRoom.connect[dIdx] = true;
                        adjRoom.connect[dOppIdx] = true;
                    }
                }
            }
        }

        // debug: print the topology
        //int doubleSize = 2*roomGridSize + 1;
        int debugWidth = 3*roomGridSize + 3;
        int debugHeight = 2*roomGridSize + 1;
        char[][] debugArea = new char[debugWidth][debugHeight];
        for (int x = 0; x < debugWidth; x++) {
            for (int y = 0; y < debugHeight; y++) {
                debugArea[x][y] = ' ';
            }
        }
        for (int x = 0; x < roomGridSize; x++) {
            for (int y = 0; y < roomGridSize; y++) {
                if (connections[x][y] != null) {
                    RoomConnection room = connections[x][y];
                    char roomChar0 = (char) ((room.level % 10) + '0');
                    char roomChar1 = ' ';
                    if (room.level >= 10) {
                        int tens = (room.level / 10) % 10;
                        roomChar1 = (char) (tens + '0');
                    }
                    debugArea[3 * x + 1][2*y + 1] = roomChar1;
                    debugArea[3 * x + 2][2*y + 1] = roomChar0;
                    if (room.connect[DirectionSets.Cardinal.N]) debugArea[3*x + 2][2*y  ] = '|';
                    if (room.connect[DirectionSets.Cardinal.S]) debugArea[3*x + 2][2*y+2] = '|';
                    if (room.connect[DirectionSets.Cardinal.W]) debugArea[3*x    ][2*y+1] = '-';
                    if (room.connect[DirectionSets.Cardinal.E]) debugArea[3*x + 3][2*y+1] = '-';
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < debugHeight; y++) {
            for (int x = 0; x < debugWidth; x++) {
                sb.append(debugArea[x][y]);
            }
            sb.append('\n');
        }
        System.out.println(sb.toString());

        return rooms;
    }

}
