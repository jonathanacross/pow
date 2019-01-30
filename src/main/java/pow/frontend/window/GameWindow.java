package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class GameWindow extends AbstractWindow {

    private final Deque<AbstractWindow> layers;
    public final int MESSAGE_BAR_HEIGHT = 40;  // height of the info bar at the bottom.

    public GameWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        GameMainLayer mainLayer = new GameMainLayer(this);
        layers = new ArrayDeque<>();
        layers.push(mainLayer);
    }

    public void removeLayer() {
        layers.pop();
        frontend.setDirty(true);
    }

    public void addLayer(AbstractWindow layer) {
        layers.push(layer);
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
        // Draw from bottom to top
        Iterator<AbstractWindow> i = layers.descendingIterator();
        while (i.hasNext()) {
            i.next().drawContents(graphics);
        }
    }

}
