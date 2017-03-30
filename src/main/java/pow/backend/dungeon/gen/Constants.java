package pow.backend.dungeon.gen;

// Constants to store terrain/feature data while constructing a map.
public class Constants {

    public static final int TERRAIN_WALL = 1;
    public static final int TERRAIN_FLOOR = 2;
    public static final int TERRAIN_DIGGABLE_WALL = 3;
    public static final int TERRAIN_LAVA = 6;
    public static final int TERRAIN_WATER = 7;
    public static final int TERRAIN_TEMP = 98;
    public static final int TERRAIN_DEBUG = 99;

    public static final int FEATURE_NONE = 0;
    public static final int FEATURE_CLOSED_DOOR = 1 << 8;
    public static final int FEATURE_OPEN_DOOR = 2 << 8;
    public static final int FEATURE_CANDLE = 3 << 8;
    public static final int FEATURE_UP_STAIRS = 4 << 8;
    public static final int FEATURE_DOWN_STAIRS = 5 << 8;
    public static final int FEATURE_FOUNTAIN = 6 << 8;
    public static final int FEATURE_INN_DOOR = 7 << 8;
    public static final int FEATURE_MAGIC_SHOP_DOOR = 8 << 8;
    public static final int FEATURE_WEAPON_SHOP_DOOR = 9 << 8;

    public static int getTerrain(int x) { return x & 0xff; }
    public static int getFeature(int x) { return x & 0xff00; }


    // character representation of the constants above.
    // This is convenient for hand made maps, but it's lossy.
    public static char getChar(int x) {
        int feature = getFeature(x);
        if (feature != Constants.FEATURE_NONE) {
            switch (feature) {
                case Constants.FEATURE_CLOSED_DOOR: return '+';
                case Constants.FEATURE_OPEN_DOOR: return '\'';
                case Constants.FEATURE_CANDLE: return 'c';
                case Constants.FEATURE_UP_STAIRS: return '<';
                case Constants.FEATURE_DOWN_STAIRS: return '>';
                case Constants.FEATURE_FOUNTAIN: return 'f';
                case Constants.FEATURE_INN_DOOR: return '1';
                case Constants.FEATURE_WEAPON_SHOP_DOOR: return '2';
                case Constants.FEATURE_MAGIC_SHOP_DOOR: return '3';
                default: throw new IllegalArgumentException("unknown feature " + feature);
            }
        } else {
            int terrain = Constants.getTerrain(x);
            switch (terrain) {
                case Constants.TERRAIN_WALL: return '#';
                case Constants.TERRAIN_FLOOR: return '.';
                case Constants.TERRAIN_DIGGABLE_WALL: return '%';
                case Constants.TERRAIN_LAVA: return '~';
                case Constants.TERRAIN_WATER: return 'w';
                case Constants.TERRAIN_DEBUG: return '?';
                default: throw new IllegalArgumentException("unknown terrain " + terrain);
            }
        }
    }

    // Opposite of getChar.  We have to assume the terrain type
    // if we see a feature character.
    public static int parseChar(char x) {
        switch (x) {
            case '#': return Constants.TERRAIN_WALL;
            case '.': return Constants.TERRAIN_FLOOR;
            case '%': return Constants.TERRAIN_DIGGABLE_WALL;
            case '~': return Constants.TERRAIN_LAVA;
            case 'w': return Constants.TERRAIN_WATER;
            case '?': return Constants.TERRAIN_DEBUG;
            case '+': return Constants.TERRAIN_FLOOR + Constants.FEATURE_CLOSED_DOOR;
            case '\'': return Constants.TERRAIN_FLOOR + Constants.FEATURE_OPEN_DOOR;
            case 'c': return Constants.TERRAIN_WALL + Constants.FEATURE_CANDLE;
            case '<': return Constants.TERRAIN_FLOOR + Constants.FEATURE_UP_STAIRS;
            case '>': return Constants.TERRAIN_FLOOR + Constants.FEATURE_DOWN_STAIRS;
            case 'f': return Constants.TERRAIN_FLOOR + Constants.FEATURE_FOUNTAIN;
            case '1': return Constants.TERRAIN_FLOOR + Constants.FEATURE_INN_DOOR;
            case '2': return Constants.TERRAIN_FLOOR + Constants.FEATURE_WEAPON_SHOP_DOOR;
            case '3': return Constants.TERRAIN_FLOOR + Constants.FEATURE_MAGIC_SHOP_DOOR;
            default: throw new IllegalArgumentException("unknown char " + x);
        }
    }
}
