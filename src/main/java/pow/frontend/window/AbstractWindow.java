package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public abstract class AbstractWindow {
    public final WindowDim dim;
    protected boolean visible;
    protected final GameBackend backend;
    protected final Frontend frontend;

    public AbstractWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        this.dim = dim;
        this.visible = visible;
        this.backend = backend;
        this.frontend = frontend;
    }

    public void resize(int width, int height) {
        this.dim.width = width;
        this.dim.height = height;
    }

    public void move(int x, int y) {
        this.dim.x = x;
        this.dim.y = y;
    }

    // to be filled out by subclasses
    // TODO: make processKey be a bool -- returns true if there's a backend change
    public abstract void processKey(KeyEvent e);
    protected abstract void drawContents(Graphics graphics);

    private void drawFrame(Graphics graphics) {
        int margin = 1;
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawRect(dim.x - margin, dim.y - margin, dim.width + margin, dim.height + margin);
    }

    public void draw(Graphics graphics) {
        if (this.visible) {
            drawFrame(graphics);
            Graphics contentGraphics = graphics.create(dim.x, dim.y, dim.width, dim.height);
            drawContents(contentGraphics);
        }
    }
}
