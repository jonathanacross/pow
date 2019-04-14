package pow.frontend.widget;

import java.awt.Graphics;

public class Space implements Widget {
    private final int width;
    private final int height;

    public Space(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Space() {
        this(0, 0);
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {}

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
}
