package pow.frontend;

import pow.frontend.window.GameWindow;

public class WindowLayout {
    private int width;
    private int height;

    // Dimensions for the windows.
    //  +-------+ +-----------------+ +-------+
    //  |       | |                 | |       |
    //  |       | |                 | |mapPane|
    //  |status | |  centerPane     | |       |
    //  | Pane  | |                 | +-------+
    //  |       | |                 | +-------+
    //  |       | |                 | |       |
    //  |       | |                 | |logPane|
    //  |       | |                 | |       |
    //  |       | |                 | |       |
    //  +-------+ +-----------------+ +-------+
    private WindowDim statusPaneDim;
    private WindowDim centerPaneDim;
    private WindowDim mapPaneDim;
    private WindowDim logPaneDim;

    public WindowDim center(int windowWidth, int windowHeight) {
        int x = (width - windowWidth) / 2;
        int y = (height - windowHeight) / 3; // don't vertically center perfectly; weight toward the top
        return new WindowDim(x, y, windowWidth, windowHeight);
    }

    public WindowDim getStatusPaneDim() { return statusPaneDim; }
    public WindowDim getCenterPaneDim() { return centerPaneDim; }
    public WindowDim getMapPaneDim() { return mapPaneDim; }
    public WindowDim getLogPaneDim() { return logPaneDim; }

    public WindowLayout(int width, int height) {
        setScreenSize(width, height);
    }

    public void setScreenSize(int width, int height) {
        this.width = width;
        this.height = height;

        int centerPaneHeight = getCenterPaneHeight();
        int centerPaneWidth = getCenterPaneWidth();
        int statusPaneWidth = 200;
        int statusPaneHeight = centerPaneHeight;
        int mapAndLogPaneLeft = 3*Style.WINDOW_SPACING + statusPaneWidth + centerPaneWidth;
        int mapPaneWidth = width - 4*Style.WINDOW_SPACING - statusPaneWidth - centerPaneWidth;
        int mapPaneHeight = centerPaneHeight / 3;
        int logPaneWiidth = mapPaneWidth;
        int logPaneHeight = centerPaneHeight - Style.WINDOW_SPACING - mapPaneHeight;

        statusPaneDim = new WindowDim(Style.WINDOW_SPACING, Style.WINDOW_SPACING, statusPaneWidth, statusPaneHeight);
        centerPaneDim = new WindowDim(statusPaneWidth + 2*Style.WINDOW_SPACING,
                Style.WINDOW_SPACING, centerPaneWidth, centerPaneHeight);
        mapPaneDim = new WindowDim(mapAndLogPaneLeft, Style.WINDOW_SPACING, mapPaneWidth, mapPaneHeight);
        logPaneDim = new WindowDim(mapAndLogPaneLeft, 2*Style.WINDOW_SPACING + mapPaneHeight,
                logPaneWiidth, logPaneHeight);
    }

    // The height of all the windows is determined by the main game window,
    // which we desire to have the right height to fit an odd number of game
    // tiles exactly vertically.
    private int getCenterPaneHeight() {
        // adjust for margins at the top/bottom of the screen
        int maxHeight = height - 2 * Style.WINDOW_SPACING;

        // the center pane must be a multiple of TILESIZE squares wide,
        // and the same number of squares high, plus the bar at the bottom.
        int numVertTiles = (maxHeight - GameWindow.MESSAGE_BAR_HEIGHT) / Style.TILE_SIZE;
        if (numVertTiles % 2 == 0) {
            numVertTiles--;  // force an odd number of tiles so the player is centered.
        }
        int centerPaneHeight = Style.TILE_SIZE * numVertTiles + GameWindow.MESSAGE_BAR_HEIGHT;

        return centerPaneHeight;
    }

    private int getCenterPaneWidth() {
        // try to use approximately 60% of the screen
        int maxWidth = (int) Math.ceil(0.6 * width);
        int numHorizTiles = maxWidth / Style.TILE_SIZE;
        if (numHorizTiles % 2 == 0) {
            numHorizTiles--;  // force an odd number of tiles so the player is centered.
        }
        int centerPaneWidth = Style.TILE_SIZE * numHorizTiles;

        return centerPaneWidth;
    }
}
