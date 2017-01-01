package pow.frontend.window;

import pow.backend.GameState;
import pow.frontend.utils.ImageController;

import java.awt.Color;
import java.awt.Graphics;

// A utility class to aid in drawing map-related views.
public class MapView {

    // These variables are useful to restrict what tiles to draw
    // (e.g., anything outside of these bounds will not appear in the view).
    public int colMin;
    public int colMax;
    public int rowMin;
    public int rowMax;

    private int tileSize;
    private int windowShiftX;
    private int windowShiftY;
    private int cameraDx;
    private int cameraDy;

    // width, height are for the size of the view, in pixels
    public MapView(int width, int height, int tileSize, GameState gs) {

        this.tileSize = tileSize;

        // compute how many rows/columns to show
        int xRadius = (int) Math.ceil(0.5 * ((double) width / tileSize - 1));
        int yRadius = (int) Math.ceil(0.5 * ((double) height / tileSize - 1));

        // how much to shift the tiles to display centered
        this.windowShiftX = (width - (2 * xRadius + 1) * tileSize) / 2;
        this.windowShiftY = (height - (2 * yRadius + 1) * tileSize) / 2;

        int camCenterX = Math.min(Math.max(xRadius, gs.player.loc.x), gs.map.width - 1 - xRadius);
        int camCenterY = Math.min(Math.max(yRadius, gs.player.loc.y), gs.map.height - 1 - yRadius);

        colMin = Math.max(0, camCenterX - xRadius);
        colMax = Math.min(gs.map.width - 1, camCenterX + xRadius);
        rowMin = Math.max(0, camCenterY - xRadius);
        rowMax = Math.min(gs.map.height - 1, camCenterY + xRadius);

        cameraDx = -(colMin + colMax) / 2 + xRadius;
        cameraDy = -(rowMin + rowMax) / 2 + yRadius;
    }

    public void frameRect(Graphics graphics, Color color, int x, int y) {
        graphics.setColor(color);
        graphics.drawRect(gameXToPixelX(x), gameYToPixelY(y), tileSize, tileSize);
    }

    public void drawTile(Graphics graphics, String tileName, int x, int y) {
        ImageController.drawTile(graphics, tileName, gameXToPixelX(x), gameYToPixelY(y), false, tileSize);
    }

    public void drawBlock(Graphics graphics, Color color, int x, int y) {
        graphics.setColor(color);
        graphics.fillRect(gameXToPixelX(x), gameYToPixelY(y), tileSize, tileSize);
    }

    // darkness = 0 -> no shadow, darkness 255 -> all black
    public void makeShadow(Graphics graphics, int x, int y, int darkness) {
        Color black = new Color(0,0, 0, darkness);
        graphics.setColor(black);
        graphics.fillRect(gameXToPixelX(x), gameYToPixelY(y), tileSize, tileSize);
    }

    private int gameXToPixelX(int x) {
        return (x + cameraDx)*tileSize + windowShiftX;
    }

    private int gameYToPixelY(int y) {
        return (y + cameraDy)*tileSize + windowShiftY;
    }
}
