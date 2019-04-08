package pow.frontend.widget;

import pow.frontend.Style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

// Widget to hold rotated text.  Note that the text itself is not used
// to store dimension information; this is passed in.
// Note that for simplicity, this only handles rotation of -45 degrees,
// or going in the NE direction.
public class RotatedText implements Widget {

    private final int width;
    private final int height;
    private final String text;
    private final Font font;

    public RotatedText(int width, int height, String text, Font font) {
        this.width = width;
        this.height = height;
        this.text = text;
        this.font = font;
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(font);
        Graphics2D g2d = (Graphics2D) graphics;

        int angle = -45;
        float textX = x + font.getSize();
        float textY = y + this.height - Style.SMALL_MARGIN;
        g2d.translate(textX, textY);
        g2d.rotate(Math.toRadians(angle));
        g2d.drawString(text,0,0);
        g2d.rotate(-Math.toRadians(angle));
        g2d.translate(-textX, -textY);
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }
}
