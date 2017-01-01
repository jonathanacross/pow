package pow.frontend.window;

import pow.backend.GameState;
import pow.frontend.utils.ImageController;
import pow.util.MathUtils;
import pow.util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class GameTargetLayer extends AbstractWindow {

    private GameWindow parent;
    private Point cursorPosition;
    MapView mapView;

    public GameTargetLayer(GameWindow parent) {
        super(parent.x, parent.y, parent.width, parent.height, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
        GameState gs = backend.getGameState();
        Point playerLoc = gs.player.getLocation();
        cursorPosition = new Point(playerLoc.x, playerLoc.y);
        mapView = new MapView(width, height, ImageController.TILE_SIZE, gs);
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

    @Override
    public void drawContents(Graphics graphics) {
        GameState gs = backend.getGameState();

        mapView.frameRect(graphics, Color.YELLOW, cursorPosition.x, cursorPosition.y);
    }

    private void moveCursor(int dx, int dy) {
        cursorPosition.x = MathUtils.clamp(cursorPosition.x + dx, mapView.colMin, mapView.colMax);
        cursorPosition.y = MathUtils.clamp(cursorPosition.y + dy, mapView.rowMin, mapView.rowMax);
        frontend.setDirty(true);
    }

    private void stopLooking() {
        parent.removeLayer();
    }
}
