package pow.frontend.window;

import pow.backend.GameState;
import pow.backend.action.FireRocket;
import pow.backend.action.Move;
import pow.backend.action.Save;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.effect.GlyphLoc;
import pow.frontend.utils.ImageController;
import pow.util.Point;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GameTargetLayer extends AbstractWindow {

    GameWindow parent;

    public GameTargetLayer(GameWindow parent) {
        super(parent.x, parent.y, parent.width, parent.height, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
        setTileSize(ImageController.TILE_SIZE);
        GameState gs = backend.getGameState();
        cursorPosition = new Point(gs.player.getLocation().x, gs.player.getLocation().y);
    }

    private Point cursorPosition;

    private void moveCursor(int dx, int dy) {
        cursorPosition.x += dx;
        cursorPosition.y += dy;
        frontend.setDirty(true);
    }

    private void stopLooking() {
        parent.removeLayer();
    }

    @Override
    public void processKey(KeyEvent e) {
        GameState gs = backend.getGameState();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_L:
                moveCursor(1, 0);
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_H:
                moveCursor(-1, 0);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
                moveCursor(0, 1);
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
                moveCursor(0, -1);
                break;
            case KeyEvent.VK_Y:
                moveCursor(-1, -1);
                break;
            case KeyEvent.VK_U:
                moveCursor(1, -1);
                break;
            case KeyEvent.VK_B:
                moveCursor(-1, 1);
                break;
            case KeyEvent.VK_N:
                moveCursor(1, 1);
                break;
            case KeyEvent.VK_PERIOD:
                moveCursor(0, 0);
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_X:
                stopLooking();
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

    private void frameRect(Graphics graphics, int x, int y) {
        graphics.setColor(Color.YELLOW);
        graphics.drawRect(x*tileSize + windowShiftX, y * tileSize + windowShiftY, tileSize, tileSize);
    }

    private int tileSize;
    private int windowShiftX;
    private int windowShiftY;
    private int xRadius;
    private int yRadius;

    // TODO: pull this out to a general map class
    int camCenterX;
    int camCenterY;

    int colMin;
    int colMax;
    int rowMin;
    int rowMax;

    int cameraDx;
    int cameraDy;

    @Override
    public void drawContents(Graphics graphics) {
        GameState gs = backend.getGameState();

        camCenterX = Math.min(Math.max(xRadius, gs.player.loc.x), gs.map.width - 1 - xRadius);
        camCenterY = Math.min(Math.max(yRadius, gs.player.loc.y), gs.map.height - 1 - yRadius);

        colMin = Math.max(0, camCenterX - xRadius);
        colMax = Math.min(gs.map.width - 1, camCenterX + xRadius);
        rowMin = Math.max(0, camCenterY - xRadius);
        rowMax = Math.min(gs.map.height - 1, camCenterY + xRadius);

        cameraDx = -(colMin + colMax) / 2 + xRadius;
        cameraDy = -(rowMin + rowMax) / 2 + yRadius;

        frameRect(graphics, cursorPosition.x + cameraDx, cursorPosition.y + cameraDy);
    }
}
