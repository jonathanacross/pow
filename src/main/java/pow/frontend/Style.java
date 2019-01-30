package pow.frontend;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

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

    private static Font smallFont;
    private static Font defaultFont;
    private static Font bigFont;
    static {
        try {
            InputStream fontStream = Style.class.getResourceAsStream("/fonts/OverpassMono-Regular.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            smallFont = customFont.deriveFont((float) SMALL_FONT_SIZE);
            defaultFont = customFont.deriveFont((float) FONT_SIZE);
            bigFont = customFont.deriveFont((float) BIG_FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            System.out.println("Couldn't load font, using default.");
            e.printStackTrace();
            smallFont = new Font(Font.MONOSPACED, Font.PLAIN, SMALL_FONT_SIZE);
            defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE);
            bigFont = new Font(Font.MONOSPACED, Font.PLAIN, BIG_FONT_SIZE);
        }
    }

    public static Font getSmallFont() { return smallFont; }
    public static Font getDefaultFont() { return defaultFont; }
    public static Font getBigFont() { return bigFont; }

    // Colors
    public static final Color SEPARATOR_LINE_COLOR = Color.DARK_GRAY;
}
