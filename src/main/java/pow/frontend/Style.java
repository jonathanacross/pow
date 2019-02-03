package pow.frontend;

import java.awt.Color;
import java.awt.Font;

public class Style {

    // Size of text.
    public static final int SMALL_FONT_SIZE = 9;
    public static final int FONT_SIZE = 12;
    public static final int BIG_FONT_SIZE = 18;

    // Space to leave around window border.
    public static final int SMALL_MARGIN = 10;
    public static final int MARGIN = 20;

    // Line kerning.
    public static final int FONT_SPACING = FONT_SIZE + 5;

    // Size of tiles.
    public static final int TILE_SIZE = 32;
    public static final int MAP_TILE_SIZE = 4;

    private static final Font smallFont = new Font("Courier", Font.PLAIN, SMALL_FONT_SIZE);
    private static final Font defaultFont = new Font("Courier", Font.PLAIN, FONT_SIZE);
    private static final Font bigFont = new Font("Courier", Font.PLAIN, BIG_FONT_SIZE);

    public static Font getSmallFont() { return smallFont; }
    public static Font getDefaultFont() { return defaultFont; }
    public static Font getBigFont() { return bigFont; }

    // Window placement
    public static int WINDOW_SPACING = 5;

    // Colors
    public static final Color SEPARATOR_LINE_COLOR = Color.DARK_GRAY;
}
