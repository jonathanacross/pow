package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Stack;

public class GameWindow extends AbstractWindow {

    private Stack<AbstractWindow> layers;

    private GameMainLayer mainLayer;

    public GameWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
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
