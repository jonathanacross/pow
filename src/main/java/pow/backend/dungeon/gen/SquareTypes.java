package pow.backend.dungeon.gen;

public enum SquareTypes {
    WALL(0, '#', "floor", null),
    FLOOR(1, '.', "wall", null),
    DIGGABLEWALL(2, '%', "diggable wall", null),
    CLOSEDDOOR(3, '+', "floor", "closed door"),
    OPENDOOR(4, '\'', "floor", "open door"),
    CANDLEWALL(5, 'c', "wall", "candle"),
    LAVA(6, '~', "lava", null),
    WATER(7, 'w', "water", null),
    TEMP(99, '?', "debug", null);

    private int value;
    private char displayChar;
    private String terrainName;
    private String featureName;

    public int value() { return value; }
    public char displayChar() { return displayChar; }
    public String terrainName() { return terrainName; }
    public String featureName() { return featureName; }

    SquareTypes(int value, char displayChar, String terrainName, String featureName) {
        this.value = value;
        this.displayChar = displayChar;
        this.terrainName = terrainName;
        this.featureName = featureName;
    }

    public static SquareTypes fromDisplayChar(char c) {
        for (SquareTypes s : values()) {
            if (s.displayChar() == c) {
                return s;
            }
        }
        return null;
    }

    public static SquareTypes fromValue(int v) {
        for (SquareTypes s : values()) {
            if (s.value() == v) {
                return s;
            }
        }
        return null;
    }
}
