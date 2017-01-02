package pow.frontend.window;

import com.sun.org.apache.xalan.internal.utils.FeatureManager;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
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
        frontend.messages.push("");
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
        mapView.frameRect(graphics, Color.YELLOW, cursorPosition.x, cursorPosition.y);
    }

    private void moveCursor(int dx, int dy) {
        cursorPosition.x = MathUtils.clamp(cursorPosition.x + dx, mapView.colMin, mapView.colMax);
        cursorPosition.y = MathUtils.clamp(cursorPosition.y + dy, mapView.rowMin, mapView.rowMax);
        frontend.messages.pop();
        frontend.messages.push(makeMessage());
        Actor selectedActor = backend.getGameState().map.actorAt(cursorPosition.x, cursorPosition.y);
        frontend.monsterInfoWindow.setActor(selectedActor);
        frontend.monsterInfoWindow.visible = selectedActor != null;
        frontend.setDirty(true);
    }

    private void stopLooking() {
        frontend.messages.pop();
        frontend.monsterInfoWindow.setActor(null);
        frontend.monsterInfoWindow.visible = false;
        parent.removeLayer();
    }

    private String makeMessage() {
        int x = cursorPosition.x;
        int y = cursorPosition.y;
        GameState gs = backend.getGameState();

        if (gs.player.canSee(gs, cursorPosition)) {
            Actor actor = gs.map.actorAt(x,y);
            if (actor != null) {
                return "you see a " + actor.name;
            }
            DungeonFeature feature = gs.map.map[x][y].feature;
            if (feature != null) {
                return "you see a " + feature.name;
            }
            return "you see a " + gs.map.map[x][y].terrain.name;
        }
        else {
            if (gs.map.map[x][y].seen) {
                DungeonFeature feature = gs.map.map[x][y].feature;
                if (feature != null) {
                    return feature.name;
                }

                return gs.map.map[x][y].terrain.name;
            } else {
                return "";  // skip squares the player can't see
            }
        }
    }
}
