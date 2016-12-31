package pow.backend.dungeon.gen;

// Class to store key information in a DungeonSquare as an integer.
// Useful for dungeon generation where we don't care about detailed
// information on each square.
public class IntSquare {

    // TODO: at the cost of being more verbose, it may be worthwhile
    // putting these into two enums.  (If this is done, it will also
    // clean up the dungeon debug printing code.)
    public static final int WALL = 0;
    public static final int FLOOR = 1;
    public static final int DIGGABLE_WALL = 2;
    public static final int LAVA = 6;
    public static final int WATER = 7;
    public static final int DEBUG = 99;

    public static final int NO_FEATURE = 0 << 8;
    public static final int CLOSED_DOOR = 1 << 8;
    public static final int OPEN_DOOR = 2 << 8;
    public static final int CANDLE = 3 << 8;
    public static final int WIN = 98 << 8;
    public static final int LOSE = 99 << 8;

    public static int getTerrain(int x) { return x & 0xff; }
    public static int getFeature(int x) { return x & 0xff00; }
}
