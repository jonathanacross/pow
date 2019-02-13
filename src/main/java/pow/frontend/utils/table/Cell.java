package pow.frontend.utils.table;

import java.awt.Graphics;

public interface Cell {
    void draw(Graphics graphics, int x, int y, int cellWidth, int cellHeight);

    int getHeight();
    int getWidth();
}
