package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

public abstract class AbstractWindow {
    public WindowDim dim;
    protected boolean visible;
    protected final GameBackend backend;
    protected final Frontend frontend;

    public AbstractWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        this.dim = dim;
        this.visible = visible;
        this.backend = backend;
        this.frontend = frontend;
    }

    public void resize(WindowDim dim) {
        this.dim.x = dim.x;
        this.dim.y = dim.y;
        this.dim.width = dim.width;
        this.dim.height = dim.height;
    }

    // to be filled out by subclasses
    // TODO: make processKey be a bool -- returns true if there's a backend change
    public abstract void processKey(KeyEvent e);
    protected abstract void drawContents(Graphics graphics);

    private void drawFrame(Graphics graphics) {
        int margin = 1;
        graphics.setColor(Style.WINDOW_FRAME_COLOR);
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
