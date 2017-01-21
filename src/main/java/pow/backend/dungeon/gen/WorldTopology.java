package pow.backend.dungeon.gen;

import pow.util.direction.DirectionSets;

import java.util.Arrays;

// class that describes how maps fit together.
public class WorldTopology {
    public static class RoomConnection {
        public int level;
        public boolean[] connect;
        public int[] adjroomIdx;  // index (level) of rooms in the cardinal directions
        public int x;
        public int y;

        public RoomConnection(int x, int y) {
            level = -1;
            int numDirs = DirectionSets.Cardinal.size();
            connect = new boolean[numDirs];
            adjroomIdx = new int[numDirs];
            Arrays.fill(adjroomIdx, -1);
            this.x = x;
            this.y = y;
        }
    }

}
