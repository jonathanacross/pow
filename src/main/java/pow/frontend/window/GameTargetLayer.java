package pow.frontend.window;

import pow.backend.GameState;
import pow.frontend.utils.ImageController;
import pow.util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class GameTargetLayer extends AbstractWindow {

    private GameWindow parent;
    private Point cursorPosition;

    public GameTargetLayer(GameWindow parent) {
        super(parent.x, parent.y, parent.width, parent.height, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
        Point playerLoc = backend.getGameState().player.getLocation();
        cursorPosition = new Point(playerLoc.x, playerLoc.y);
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
        MapView mapView = new MapView(width, height, ImageController.TILE_SIZE, gs);

        mapView.frameRect(graphics, Color.YELLOW, cursorPosition.x, cursorPosition.y);
    }

    private void moveCursor(int dx, int dy) {
        cursorPosition.x += dx;
        cursorPosition.y += dy;
        frontend.setDirty(true);
    }

    private void stopLooking() {
        parent.removeLayer();
    }
}
