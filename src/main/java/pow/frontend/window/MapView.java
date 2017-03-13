package pow.frontend.window;

import pow.backend.GameState;
import pow.frontend.utils.ImageController;
import pow.util.MathUtils;

import java.awt.*;

// A utility class to aid in drawing map-related views.
public class MapView {

    // These variables are useful to restrict what tiles to draw
    // (e.g., anything outside of these bounds will not appear in the view).
    public final int colMin;
    public final int colMax;
    public final int rowMin;
    public final int rowMax;

    private final int tileSize;
    private final int windowShiftX;
    private final int windowShiftY;
    private final int cameraDx;
    private final int cameraDy;

    // width, height are for the size of the view, in pixels
    public MapView(int width, int height, int tileSize, GameState gs) {

        this.tileSize = tileSize;

        // compute how many rows/columns to show
        int xRadius = (int) Math.ceil(0.5 * ((double) width / tileSize - 1));
        int yRadius = (int) Math.ceil(0.5 * ((double) height / tileSize - 1));

        // how much to shift the tiles to display centered
        this.windowShiftX = (width - (2 * xRadius + 1) * tileSize) / 2;
        this.windowShiftY = (height - (2 * yRadius + 1) * tileSize) / 2;

        int camCenterX = MathUtils.clamp(gs.player.loc.x, xRadius, gs.getCurrentMap().width - 1 - xRadius);
        int camCenterY = MathUtils.clamp(gs.player.loc.y, yRadius, gs.getCurrentMap().height - 1 - yRadius);

        colMin = Math.max(0, camCenterX - xRadius);
        colMax = Math.min(gs.getCurrentMap().width - 1, camCenterX + xRadius);
        rowMin = Math.max(0, camCenterY - yRadius);
        rowMax = Math.min(gs.getCurrentMap().height - 1, camCenterY + yRadius);

        cameraDx = -(colMin + colMax) / 2 + xRadius;
        cameraDy = -(rowMin + rowMax) / 2 + yRadius;
    }

    public void frameRect(Graphics graphics, Color color, int x, int y) {
        graphics.setColor(color);
        graphics.drawRect(gameXToPixelX(x), gameYToPixelY(y), tileSize - 1, tileSize - 1);
    }

    public void drawTile(Graphics graphics, String tileName, int x, int y) {
        ImageController.drawTile(graphics, tileName, gameXToPixelX(x), gameYToPixelY(y), false, tileSize);
    }

    public void drawBlock(Graphics graphics, Color color, int x, int y) {
        graphics.setColor(color);
        graphics.fillRect(gameXToPixelX(x), gameYToPixelY(y), tileSize, tileSize);
    }

    public void drawCircle(Graphics graphics, Color color, int x, int y) {
        Graphics2D g2 = (Graphics2D) graphics;
        int tileCenterX = gameXToPixelX(x) + tileSize / 2;
        int tileCenterY = gameYToPixelY(y) + tileSize / 2;
        int radius = (int) Math.round((double) tileSize / 6.4);
        int diam = 2*radius;

        g2.setStroke(new BasicStroke(4));
        graphics.setColor(Color.BLACK);
        graphics.drawOval(tileCenterX - radius, tileCenterY - radius, diam, diam);

        g2.setStroke(new BasicStroke(2));
        graphics.setColor(color);
        graphics.drawOval(tileCenterX - radius, tileCenterY - radius, diam, diam);
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
