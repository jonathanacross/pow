package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.action.*;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.Frontend;
import pow.frontend.effect.GlyphLoc;
import pow.frontend.utils.ImageController;
import pow.util.Circle;
import pow.util.Point;
import pow.util.DebugLogger;
import pow.util.MathUtils;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GameWindow extends AbstractWindow {

    public GameWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
        setTileSize(ImageController.TILE_SIZE);
    }

    @Override
    public void processKey(KeyEvent e) {
        GameState gs = backend.getGameState();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_L:
                backend.tellPlayer(new Move(gs.player, 1, 0));
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_H:
                backend.tellPlayer(new Move(gs.player, -1, 0));
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
                backend.tellPlayer(new Move(gs.player, 0, 1));
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
                backend.tellPlayer(new Move(gs.player, 0, -1));
                break;
            case KeyEvent.VK_Y:
                backend.tellPlayer(new Move(gs.player, -1, -1));
                break;
            case KeyEvent.VK_U:
                backend.tellPlayer(new Move(gs.player, 1, -1));
                break;
            case KeyEvent.VK_B:
                backend.tellPlayer(new Move(gs.player, -1, 1));
                break;
            case KeyEvent.VK_N:
                backend.tellPlayer(new Move(gs.player, 1, 1));
                break;
            case KeyEvent.VK_PERIOD:
                // TODO: change to rest
                backend.tellPlayer(new Move(gs.player, 0, 0));
                break;
            case KeyEvent.VK_F:
                backend.tellPlayer(new FireRocket(gs.player));
                break;
            case KeyEvent.VK_S:
                backend.tellPlayer(new Save());
                break;
        }
    }

    // Used to figure out how much we can show on the map.
    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;

        // compute how many rows/columns to show
        this.xRadius = (int) Math.ceil(0.5 * ((double) width / tileSize - 1));
        this.yRadius = (int) Math.ceil(0.5 * ((double) height / tileSize - 1));

        // how much to shift the tiles to display centered
        this.windowShiftX = (width - (2 * xRadius + 1) * tileSize) / 2;
        this.windowShiftY = (height - (2 * yRadius + 1) * tileSize) / 2;
    }

    private void drawTile(Graphics graphics, String tileName, int x, int y) {
        ImageController.drawTile(graphics, tileName, x * tileSize + windowShiftX, y * tileSize + windowShiftY);
    }

    // darkness = 0 -> no shadow, darkness 255 -> all black
    private void makeShadow(Graphics graphics, int x, int y, int darkness) {
        Color black = new Color(0,0, 0, darkness);
        graphics.setColor(black);
        graphics.fillRect(x*tileSize + windowShiftX, y * tileSize + windowShiftY, tileSize, tileSize);
    }

    private int tileSize;
    private int windowShiftX;
    private int windowShiftY;
    private int xRadius;
    private int yRadius;

    @Override
    public void drawContents(Graphics graphics) {
        GameState gs = backend.getGameState();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);

        int camCenterX = Math.min(Math.max(xRadius, gs.player.loc.x), gs.map.width - 1 - xRadius);
        int camCenterY = Math.min(Math.max(yRadius, gs.player.loc.y), gs.map.height - 1 - yRadius);

        int colMin = Math.max(0, camCenterX - xRadius);
        int colMax = Math.min(gs.map.width - 1, camCenterX + xRadius);
        int rowMin = Math.max(0, camCenterY - xRadius);
        int rowMax = Math.min(gs.map.height - 1, camCenterY + xRadius);

        int cameraDx = -(colMin + colMax) / 2 + xRadius;
        int cameraDy = -(rowMin + rowMax) / 2 + yRadius;

        Font f = new Font("Courier New", Font.PLAIN, this.tileSize);
        graphics.setFont(f);

        // draw the map
        for (int y = rowMin; y <= rowMax; y++) {
            for (int x = colMin; x <= colMax; x++) {
                DungeonSquare square = gs.map.map[x][y];
                if (!gs.map.seen[x][y]) {
                    continue;
                }
//                if (!gs.player.canSee(gs, new Point(x,y))) {
//                    continue;
//                }
                drawTile(graphics, square.terrain.image, x + cameraDx, y + cameraDy);
                if (square.feature != null) {
                    drawTile(graphics, square.feature.image, x + cameraDx, y + cameraDy);
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.map.actors) {
            if (gs.player.canSee(gs, actor.loc)) {
                drawTile(graphics, actor.image, actor.loc.x + cameraDx, actor.loc.y + cameraDy);
            }
        }

        // draw effects
        if (!frontend.getEffects().isEmpty()) {
            for (GlyphLoc glyphLoc : frontend.getEffects().get(0).render()) {
                if (gs.player.canSee(gs, glyphLoc.loc)) {
                    drawTile(graphics, glyphLoc.imageName, glyphLoc.loc.x + cameraDx, glyphLoc.loc.y + cameraDy);
                }
            }
        }

        // add shadow
        for (int y = rowMin; y <= rowMax; y++) {
            for (int x = colMin; x <= colMax; x++) {
                if (!gs.map.seen[x][y]) {
                    continue;
                }
                int maxDarkness = 220;
                double darknessD = 1.0 - (gs.map.lightMap[x][y] / (double) gs.map.MAX_BRIGHTNESS);
                // Assign max darkness if we can't see it; alternatively, we could paint
                // in gray, or something.  Or just not show it at all?
                if (!gs.player.canSee(gs, new Point(x,y))) {
                    darknessD = 1;
                }
                int darkness = (int) Math.round(maxDarkness * darknessD);
                makeShadow(graphics, x + cameraDx, y + cameraDy, darkness);
            }
        }
    }
}
