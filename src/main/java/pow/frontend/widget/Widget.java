package pow.frontend.widget;

import java.awt.Graphics;

public interface Widget {
    // draws the contents at the location x, y.
    void draw(Graphics graphics, int x, int y);

    int getHeight();
    int getWidth();
}
