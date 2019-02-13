package pow.frontend;

import pow.frontend.utils.PropertyController;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

public class Style {

    // Size of text.
    private static final int SMALL_FONT_SIZE = 9;
    private static final int FONT_SIZE = 12;
    private static final int BIG_FONT_SIZE = 18;

    // Space to leave around window border.
    public static final int SMALL_MARGIN = 10;
    public static final int MARGIN = 20;

    // Size of tiles.
    public static final int TILE_SIZE = 32;
    public static final int MAP_TILE_SIZE = 4;

    private static Font smallFont;
    private static Font defaultFont;
    private static Font bigFont;

    private static int fontSize;

    static {
        String fontName = null;
        fontSize = FONT_SIZE;
        try {
            PropertyController propertyController = new PropertyController();
            fontName = propertyController.getProperty("font.name");
            fontSize = Integer.parseInt(propertyController.getProperty("font.size"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (fontName == null) {
            fontName = "Courier";
        }

        smallFont = new Font(fontName, Font.PLAIN, SMALL_FONT_SIZE);
        defaultFont = new Font(fontName, Font.PLAIN, fontSize);
        bigFont = new Font(fontName, Font.PLAIN, BIG_FONT_SIZE);
    }

    public static Font getSmallFont() { return smallFont; }
    public static Font getDefaultFont() { return defaultFont; }
    public static Font getBigFont() { return bigFont; }

    public static int getSmallFontSize() { return SMALL_FONT_SIZE; }
    public static int getFontSize() { return fontSize; }
    public static int getBigFontSize() { return BIG_FONT_SIZE; }

    // Window appearance and colors.
    public static final int WINDOW_SPACING = 5;

    public static final Color WINDOW_FRAME_COLOR = Color.DARK_GRAY;
    public static final Color DESKTOP_COLOR = Color.BLACK;
    public static final Color BACKGROUND_COLOR = new Color(20, 20, 20);
    public static final Color SEPARATOR_LINE_COLOR = WINDOW_FRAME_COLOR;
}
