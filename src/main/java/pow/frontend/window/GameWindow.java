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
import java.util.Stack;

public class GameWindow extends AbstractWindow {

    private Stack<AbstractWindow> layers;

    private GameMainLayer mainLayer;

    public GameWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
        mainLayer = new GameMainLayer(this);
        layers = new Stack<>();
        layers.add(mainLayer);
    }

    public void removeLayer() {
        layers.pop();
        frontend.setDirty(true);
    }

    public void addLayer(AbstractWindow layer) {
        layers.add(layer);
        frontend.setDirty(true);
    }

    @Override
    public void processKey(KeyEvent e) {
        if (!layers.isEmpty()) {
            layers.peek().processKey(e);
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        for (AbstractWindow w : layers) {
            w.drawContents(graphics);
        }
    }

}
