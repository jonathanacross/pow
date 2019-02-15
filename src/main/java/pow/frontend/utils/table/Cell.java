package pow.frontend.utils.table;

import java.awt.Graphics;

public interface Cell {
    // draws the contents at the location x, y.
    void draw(Graphics graphics, int x, int y);

    int getHeight();
    int getWidth();
}
