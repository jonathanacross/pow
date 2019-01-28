package pow.frontend;

import java.awt.Font;

public class Style {

    // Size of text.
    public static final int SMALL_FONT_SIZE = 9;
    public static final int FONT_SIZE = 12;
    public static final int LARGE_FONT_SIZE = 14;
    public static final int BIG_FONT_SIZE = 18;

    // Space to leave around window border.
    public static final int SMALL_MARGIN = 10;
    public static final int MARGIN = 20;

    // Line kerning.
    public static final int FONT_SPACING = FONT_SIZE + 5;

    // Size of tiles.
    public static final int TILE_SIZE = 32;
    public static final int MAP_TILE_SIZE = 4;

    private static final String FONT_NAME = "Courier";

    private static final Font smallFont = new Font(FONT_NAME, Font.PLAIN, SMALL_FONT_SIZE);
    private static final Font defaultFont = new Font(FONT_NAME, Font.PLAIN, FONT_SIZE);
    private static final Font largeFont = new Font(FONT_NAME, Font.PLAIN, LARGE_FONT_SIZE);
    private static final Font bigFont = new Font(FONT_NAME, Font.PLAIN, BIG_FONT_SIZE);

    public static Font getSmallFont() { return smallFont; }
    public static Font getDefaultFont() { return defaultFont; }
    public static Font getLargeFont() { return largeFont; }
    public static Font getBigFont() { return bigFont; }
}
