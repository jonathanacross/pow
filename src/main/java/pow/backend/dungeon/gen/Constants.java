package pow.backend.dungeon.gen;

// Constants to store terrain/feature data while constructing a map.
public class Constants {

    public static final int TERRAIN_WALL = 1;
    public static final int TERRAIN_FLOOR = 2;
    public static final int TERRAIN_DIGGABLE_WALL = 3;
    public static final int TERRAIN_LAVA = 6;
    public static final int TERRAIN_WATER = 7;
    public static final int TERRAIN_NOTHING = 8;
    public static final int TERRAIN_TEMP = 98;
    public static final int TERRAIN_DEBUG = 99;

    public static final int FEATURE_NONE = 0;
    public static final int FEATURE_CLOSED_DOOR = 1 << 8;
    public static final int FEATURE_OPEN_DOOR = 2 << 8;
    public static final int FEATURE_CANDLE = 3 << 8;
    public static final int FEATURE_CANDELABRA = 4 << 8;
    public static final int FEATURE_WOOD_CHEST = 5 << 8;
    public static final int FEATURE_CRATE = 6 << 8;
    public static final int FEATURE_GLASS_ORB = 7 << 8;
    public static final int FEATURE_BOOKCASE = 8 << 8;
    public static final int FEATURE_RED_CURTAIN = 9 << 8;
    public static final int FEATURE_BLUE_CURTAIN = 10 << 8;
    public static final int FEATURE_GREEN_CURTAIN = 11 << 8;
    public static final int FEATURE_RED_CARPET = 12 << 8;
    public static final int FEATURE_THRONE = 13 << 8;
    public static final int FEATURE_UP_STAIRS = 14 << 8;
    public static final int FEATURE_DOWN_STAIRS = 15 << 8;
    public static final int FEATURE_FOUNTAIN = 16 << 8;
    public static final int FEATURE_INN_DOOR = 17 << 8;
    public static final int FEATURE_MAGIC_SHOP_DOOR = 18 << 8;
    public static final int FEATURE_WEAPON_SHOP_DOOR = 19 << 8;
    public static final int FEATURE_JEWELER_SHOP_DOOR = 20 << 8;
    public static final int FEATURE_OPEN_PORTAL = 21 << 8;
    public static final int FEATURE_CLOSED_PORTAL = 22 << 8;
    public static final int FEATURE_PEARL_TILE = 23 << 8;
    public static final int FEATURE_FIRE_URN = 24 << 8;

    public static final String PORTAL_KEY_LOCATION_ID = "_PORTAL_";

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
                case Constants.FEATURE_CANDELABRA: return 'A';
                case Constants.FEATURE_WOOD_CHEST: return 'B';
                case Constants.FEATURE_CRATE: return 'C';
                case Constants.FEATURE_GLASS_ORB: return 'D';
                case Constants.FEATURE_BOOKCASE: return 'E';
                case Constants.FEATURE_RED_CURTAIN: return 'F';
                case Constants.FEATURE_BLUE_CURTAIN: return 'G';
                case Constants.FEATURE_GREEN_CURTAIN: return 'H';
                case Constants.FEATURE_RED_CARPET: return 'I';
                case Constants.FEATURE_THRONE: return 'J';
                case Constants.FEATURE_UP_STAIRS: return '<';
                case Constants.FEATURE_DOWN_STAIRS: return '>';
                case Constants.FEATURE_FOUNTAIN: return 'f';
                case Constants.FEATURE_INN_DOOR: return '1';
                case Constants.FEATURE_WEAPON_SHOP_DOOR: return '2';
                case Constants.FEATURE_MAGIC_SHOP_DOOR: return '3';
                case Constants.FEATURE_JEWELER_SHOP_DOOR: return '4';
                case Constants.FEATURE_OPEN_PORTAL: return 'P';
                case Constants.FEATURE_CLOSED_PORTAL: return 'Q';
                case Constants.FEATURE_PEARL_TILE: return 'R';
                case Constants.FEATURE_FIRE_URN: return 'S';
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
                case Constants.TERRAIN_NOTHING: return '_';
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
            case '_': return Constants.TERRAIN_NOTHING;
            case '?': return Constants.TERRAIN_DEBUG;
            case '+': return Constants.TERRAIN_FLOOR + Constants.FEATURE_CLOSED_DOOR;
            case '\'': return Constants.TERRAIN_FLOOR + Constants.FEATURE_OPEN_DOOR;
            case 'c': return Constants.TERRAIN_WALL + Constants.FEATURE_CANDLE;
            case 'A': return Constants.TERRAIN_WALL + Constants.FEATURE_CANDELABRA;
            case 'B': return Constants.TERRAIN_FLOOR + Constants.FEATURE_WOOD_CHEST;
            case 'C': return Constants.TERRAIN_FLOOR + Constants.FEATURE_CRATE;
            case 'D': return Constants.TERRAIN_FLOOR + Constants.FEATURE_GLASS_ORB;
            case 'E': return Constants.TERRAIN_FLOOR + Constants.FEATURE_BOOKCASE;
            case 'F': return Constants.TERRAIN_WALL + Constants.FEATURE_RED_CURTAIN;
            case 'G': return Constants.TERRAIN_WALL + Constants.FEATURE_BLUE_CURTAIN;
            case 'H': return Constants.TERRAIN_WALL + Constants.FEATURE_GREEN_CURTAIN;
            case 'I': return Constants.TERRAIN_FLOOR + Constants.FEATURE_RED_CARPET;
            case 'J': return Constants.TERRAIN_FLOOR + Constants.FEATURE_THRONE;
            case 'P': return Constants.TERRAIN_FLOOR + Constants.FEATURE_OPEN_PORTAL;
            case 'Q': return Constants.TERRAIN_FLOOR + Constants.FEATURE_CLOSED_PORTAL;
            case 'R': return Constants.TERRAIN_FLOOR + Constants.FEATURE_PEARL_TILE;
            case 'S': return Constants.TERRAIN_FLOOR + Constants.FEATURE_FIRE_URN;
            case '<': return Constants.TERRAIN_FLOOR + Constants.FEATURE_UP_STAIRS;
            case '>': return Constants.TERRAIN_FLOOR + Constants.FEATURE_DOWN_STAIRS;
            case 'f': return Constants.TERRAIN_FLOOR + Constants.FEATURE_FOUNTAIN;
            case '1': return Constants.TERRAIN_FLOOR + Constants.FEATURE_INN_DOOR;
            case '2': return Constants.TERRAIN_FLOOR + Constants.FEATURE_WEAPON_SHOP_DOOR;
            case '3': return Constants.TERRAIN_FLOOR + Constants.FEATURE_MAGIC_SHOP_DOOR;
            case '4': return Constants.TERRAIN_FLOOR + Constants.FEATURE_JEWELER_SHOP_DOOR;
            default: throw new IllegalArgumentException("unknown char '" + x + "'");
        }
    }
}
