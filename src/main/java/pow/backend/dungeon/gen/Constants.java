package pow.backend.dungeon.gen;

// Constants to store terrain/feature data while constructing a map.
public class Constants {

    public static final int TERRAIN_WALL = 0;
    public static final int TERRAIN_FLOOR = 1;
    public static final int TERRAIN_DIGGABLE_WALL = 2;
    public static final int TERRAIN_LAVA = 6;
    public static final int TERRAIN_WATER = 7;
    public static final int TERRAIN_DEBUG = 99;

    public static final int FEATURE_NONE = 0 << 8;
    public static final int FEATURE_CLOSED_DOOR = 1 << 8;
    public static final int FEATURE_OPEN_DOOR = 2 << 8;
    public static final int FEATURE_CANDLE = 3 << 8;
    public static final int FEATURE_UP_STAIRS = 4 << 8;
    public static final int FEATURE_DOWN_STAIRS = 5 << 8;
    public static final int FEATURE_WIN_TILE = 98 << 8;
    public static final int FEATURE_LOSE_TILE = 99 << 8;

    public static int getTerrain(int x) { return x & 0xff; }
    public static int getFeature(int x) { return x & 0xff00; }
}
