package pow.frontend.utils.table;

import java.awt.Graphics;

public class EmptyCell implements Cell {
    int width;
    int height;

    public EmptyCell(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public EmptyCell() {
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
