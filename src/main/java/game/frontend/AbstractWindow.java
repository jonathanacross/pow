package game.frontend;

import game.GameBackend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public abstract class AbstractWindow {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible;
    protected GameBackend backend;

    public AbstractWindow(int x, int y, int width, int height, boolean visible, GameBackend backend) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;
        this.backend = backend;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // to be filled out by subclasses
    public abstract void processKey(KeyEvent e);
    public abstract void drawContents(Graphics graphics);

    void drawFrame(Graphics graphics) {
        int margin = 1;
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(x - margin, y - margin, width + 2*margin, height + 2*margin);
    }

    public void draw(Graphics graphics) {
        if (this.visible) {
            drawFrame(graphics);
            Graphics contentGraphics = graphics.create(x, y, width, height);
            drawContents(contentGraphics);
        }
    }

}
