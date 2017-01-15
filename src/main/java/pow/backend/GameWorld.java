package pow.backend;

import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.gen.MapGenerator;
import pow.util.Array2D;
import pow.util.Point;
import pow.util.direction.Direction;
import pow.util.direction.DirectionNames;

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
    }

    private class RoomConnection {
        public int level;
        public boolean[] connect;
        public int x;
        public int y;

        public RoomConnection(int x, int y) {
            level = -1;
            connect = new boolean[4]; // 4 = number of cardinal directions
            // ordered by same order as DirectionNames.CARDINAL
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
        final int numLevels = 10;
        final double probMakeCycle = 0.5;

        int roomGridSize = 2 * (int) Math.sqrt(numLevels);
        RoomConnection[][] connections = new RoomConnection[roomGridSize][roomGridSize];
        List<RoomConnection> rooms = new ArrayList<>();  // same data, but just the rooms of interest

        Point start = new Point(roomGridSize / 2, roomGridSize / 2);
        connections[start.x][start.y] = new RoomConnection(start.x, start.y);
        connections[start.x][start.y].level = 0;
        rooms.add(connections[start.x][start.y]);

        // add the rest of the levels
        for (int level = 1; level < numLevels; level++) {

            int x;
            int y;
            int dir;
            RoomConnection randRoom = null;
            do {
                // pick a random room..
                randRoom = rooms.get(rng.nextInt(rooms.size()));
                // pick random direction from this room
                dir = rng.nextInt(DirectionNames.CARDINALS.length);
                Direction d = DirectionNames.getDirection(dir);
                x = randRoom.x + d.dx;
                y = randRoom.y + d.dy;
            } while (!(x >= 0 && x < roomGridSize && y >= 0 && y < roomGridSize) || connections[x][y] != null);

            // found a valid space
            RoomConnection newRoom = new RoomConnection(x,y);
            newRoom.level = level;
            connections[x][y] = newRoom;
            rooms.add(newRoom);

            // connect room to adjacent rooms
            for (int dc = 0; dc < DirectionNames.CARDINALS.length; dc++) {
                double probConnect = (dc == dir) ? 1.0 : probMakeCycle;
                if (rng.nextDouble() <= probConnect) {
                    newRoom.connect[dc] = true;
                    randRoom.connect[DirectionNames.getOpposite(dc)] = true;
                }
            }
        }

        return rooms;
    }
}
