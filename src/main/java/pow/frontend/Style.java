package pow.frontend;

import java.awt.*;

public class Style {

    // Size of text.
    public static int SMALL_FONT_SIZE = 9;
    public static int FONT_SIZE = 12;
    public static int LARGE_FONT_SIZE = 14;
    public static int BIG_FONT_SIZE = 18;

    // Space to leave around window border.
    public static int SMALL_MARGIN = 10;
    public static int MARGIN = 20;

    // Line kerning.
    public static int FONT_SPACING = FONT_SIZE + 5;

    // Size of tiles.
    public static int TILE_SIZE = 32;
    public static int MAP_TILE_SIZE = 4;

    private static Font smallFont = new Font("Courier", Font.PLAIN, SMALL_FONT_SIZE);
    private static Font defaultFont = new Font("Courier", Font.PLAIN, FONT_SIZE);
    private static Font largeFont = new Font("Courier", Font.PLAIN, LARGE_FONT_SIZE);
    private static Font bigFont = new Font("Courier", Font.PLAIN, BIG_FONT_SIZE);

    public static Font getSmallFont() { return smallFont; }
    public static Font getDefaultFont() { return defaultFont; }
    public static Font getLargeFont() { return largeFont; }
    public static Font getBigFont() { return bigFont; }
}
