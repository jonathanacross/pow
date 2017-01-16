package pow.backend.dungeon.gen;

import pow.util.direction.DirectionSets;
import pow.util.direction.Direction;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

// generates the topology of rooms/levels in the outside/overworld
public class GenOverworldTopology {

    public static class RoomConnection {
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

    private int numGroups;
    private int roomsPerGroup;
    private double probMakeCycle;

    private int numRooms;
    private int roomGridSize;
    private RoomConnection[][] connections;
    private List<RoomConnection> rooms;
    private List<List<RoomConnection>> roomsInGroup;
    private int currLevel;

    public GenOverworldTopology(Random rng) {
        this(rng, 12, 4, 0.25);
    }

    public GenOverworldTopology(Random rng, int numGroups, int roomsPerGroup, double probMakeCycle) {
        this.numGroups = numGroups;
        this.roomsPerGroup = roomsPerGroup;
        this.probMakeCycle = probMakeCycle;

        this.numRooms = numGroups * roomsPerGroup;
        this.roomGridSize = 5 * (int) Math.sqrt(numRooms);

        this.connections = new RoomConnection[roomGridSize][roomGridSize];
        this.rooms = new ArrayList<>();  // same data, but just the rooms of interest
        this.roomsInGroup = new ArrayList<>();

        this.currLevel = 0;

        genWorldTopology(rng);
    }

    public List<RoomConnection> getRooms() {
        return rooms;
    }

    // -------------- private implementation -----------

    private List<RoomConnection> genWorldTopology(Random rng) {

        // DEBUG: force a different random combination
        rng.nextInt();
        rng.nextInt();

        for (int group = 0; group < numGroups; group++) {
            addGroupOfRooms(rng, group == 0, roomsPerGroup);
        }

        return rooms;
    }

    private static class NewConnectedRoomLocation {
        public int x;  // desired coordinates
        public int y;
        public int dir;  // direction from previous room to this one.

        public NewConnectedRoomLocation(int x, int y, int directionFrom) {
            this.x = x;
            this.y = y;
            this.dir = directionFrom;
        }
    }

    // This is probabilistic; it may fail.  If this happens, then returns null.
    private NewConnectedRoomLocation getNewConnectedRoomLocation(Random rng, boolean newGroup, int maxAttempts) {

        int attempts = 0;

        // add new room to current group
        int x;
        int y;
        int dir;
        RoomConnection randRoom = null;
        do {
            randRoom = getRandomRoom(rng, newGroup);

            // pick random direction from this room
            dir = rng.nextInt(DirectionSets.Cardinal.size());
            Direction d = DirectionSets.Cardinal.getDirection(dir);
            x = randRoom.x + d.dx;
            y = randRoom.y + d.dy;
            attempts++;
        } while (attempts < maxAttempts && (!inGrid(x, y) || connections[x][y] != null));

        if (attempts >= maxAttempts) {
            return null;
        } else {
            return new NewConnectedRoomLocation(x, y, dir);
        }
    }

//    //-------------------------------------------------
//    // JC -- new implementation
//    //-------------------------------------------------
//    private List<NewConnectedRoomLocation> tryGetNewRoomGroupList(Random rng, boolean firstGroup, int numRooms) {
//
//        int[][] existingRoomGroups = new int[roomGridSize][roomGridSize];
//        for (int x = 0; x < roomGridSize; x++) {
//            for (int y = 0; y < roomGridSize; y++) {
//                existingRoomGroups[x][y] = -1;
//            }
//        }
//        int numGroupsSoFar = roomsInGroup.size();
//        for (int group = 0; group < numGroupsSoFar; group++) {
//            for (RoomConnection room : roomsInGroup.get(group)) {
//                existingRoomGroups[room.x][room.y] = group;
//            }
//        }
//
//        List<NewConnectedRoomLocation> newRooms = new ArrayList<>();
//
//        int startIdx = 0;
//        // for first room in first group, just add a room to the center
//        if (firstGroup) {
//            int x = roomGridSize / 2;
//            int y = roomGridSize / 2;
//            newRooms.add(new NewConnectedRoomLocation(x, y,-1);
//            existingRoomGroups[x][y] = numGroupsSoFar+1;
//            startIdx = 1;
//        }
//
//        for (int levelInGroup = startIdx; levelInGroup < numRooms; levelInGroup++) {
//            NewConnectedRoomLocation ncrl = getNewConnectedRoomLocation(rng, levelInGroup == 0, 100);
//
//            if (ncrl != null) {
//                // found a valid space; add a new room
//                RoomConnection newRoom = addLevel(ncrl.x, ncrl.y);
//                connectToPrevious(newRoom, ncrl.dir, rng);
//            } else {
//                // failed to get a space for a room to complete the group.
//                // Clean up our work so we can try again
//                int currGroupIdx = currGroup();
//                for (RoomConnection room : roomsInGroup.get(currGroupIdx)) {
//                    connections[room.x][room.y] = null;
//                    rooms.remove(room);
//                }
//                roomsInGroup.remove(currGroupIdx);
//                return false;
//            }
//        }
//
//        return true;
//    }

    // returns true if success
    private boolean tryAddGroupOfRooms(Random rng, boolean firstGroup, int numRooms) {
        roomsInGroup.add(new ArrayList<>());

        int startIdx = 0;
        // for first room in first group, just add a room to the center
        if (firstGroup) {
            addLevel(roomGridSize / 2, roomGridSize / 2);
            startIdx = 1;
        }

        for (int levelInGroup = startIdx; levelInGroup < numRooms; levelInGroup++) {
            NewConnectedRoomLocation ncrl = getNewConnectedRoomLocation(rng, levelInGroup == 0, 100);

            if (ncrl != null) {
                // found a valid space; add a new room
                RoomConnection newRoom = addLevel(ncrl.x, ncrl.y);
                connectToPrevious(newRoom, ncrl.dir, rng);
            } else {
                // failed to get a space for a room to complete the group.
                // Clean up our work so we can try again
                int currGroupIdx = currGroup();
                for (RoomConnection room : roomsInGroup.get(currGroupIdx)) {
                    connections[room.x][room.y] = null;
                    rooms.remove(room);
                }
                roomsInGroup.remove(currGroupIdx);
                return false;
            }
        }

        return true;
    }


    private void addGroupOfRooms(Random rng, boolean firstGroup, int numRooms) {
        final int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
           if (tryAddGroupOfRooms(rng, firstGroup, numRooms)) {
               return;
           }
        }

        // If got here, then couldn't place the group of rooms.
        // Since this algorithm is probabilistic, it is *possible* for this to happen.
        // but chances are minute.
        System.out.println(this);
        throw new RuntimeException("couldn't create overworld; could not place all rooms");
    }

    // Picks a random room to connect to.
    // If this is the beginning of a new group of rooms, then we
    // pick some level within the most recent 2 groups to branch off of
    // (motivation is to keep things from being too linear, without
    // the player having to backtrack too far).
    // If this is room is part of an existing group, then must connect
    // to another room in that group (to keep the group contiguous).
    private RoomConnection getRandomRoom(Random rng, boolean newGroup) {
        int groupIdx;
        if (newGroup) {
            // pick something from previous 2 groups.
            groupIdx = currGroup() - rng.nextInt(2) - 1;
            if (groupIdx < 0) groupIdx = 0;
        }
        else {
            groupIdx = currGroup();
        }

        List<RoomConnection> connGroup = roomsInGroup.get(groupIdx);
        RoomConnection randRoom = connGroup.get(rng.nextInt(connGroup.size()));
        return randRoom;
    }

    // adds new level to the map, in the current group
    private RoomConnection addLevel(int x, int y) {
        RoomConnection room = new RoomConnection(x, y);
        room.level = this.currLevel;
        this.currLevel++;
        this.connections[x][y] = room;
        this.rooms.add(room);
        this.roomsInGroup.get(currGroup()).add(room);
        return room;
    }

    private int currGroup() { return roomsInGroup.size() - 1; }
    private boolean inGrid(int x, int y) {
        return x >= 0 && x < roomGridSize && y >= 0 && y < roomGridSize;
    }

    // Connects a newly placed room with adjacent rooms.
    // One direction (to the previous room) is forced to make
    // a connection; other connections are made with probMakeCycle.
    // connectDirIdx is the direction from the previous room to this
    // new room.
    private void connectToPrevious(RoomConnection newRoom, int connectDirIdx, Random rng) {
        for (int dirIndex = 0; dirIndex < DirectionSets.Cardinal.size(); dirIndex++) {
            int oppDirIndex = DirectionSets.Cardinal.getOpposite(dirIndex);
            double probConnect = (connectDirIdx == oppDirIndex) ? 1.0 : probMakeCycle;
            Direction d = DirectionSets.Cardinal.getDirection(dirIndex);
            int nx = newRoom.x + d.dx;
            int ny = newRoom.y + d.dy;
            if (!inGrid(nx, ny)) continue;
            if (connections[nx][ny] == null) continue;
            RoomConnection adjRoom = connections[nx][ny];
            if (rng.nextDouble() <= probConnect) {
                newRoom.connect[dirIndex] = true;
                adjRoom.connect[oppDirIndex] = true;
            }
        }
    }

    // makes a string to show the topology; useful for debugging, primarily.
    @Override
    public String toString() {
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
        return sb.toString();
    }
}
