package pow.frontend.widget;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Bar implements Widget {
    int width;
    int height;
    int fillWidth;
    Color color;
    String text;
    Font font;
    int ascent;

    // Used to get font metrics to compute text heights
    private static Graphics fakeGraphics;
    static {
        Image dummyImage = new BufferedImage(10, 10,  BufferedImage.TYPE_INT_ARGB);
        fakeGraphics = dummyImage.getGraphics();
    }

    public Bar(int width, int fillWidth, Color color, String text, Font font) {
        FontMetrics textMetrics = fakeGraphics.getFontMetrics(font);
        this.width = width;
        this.height = textMetrics.getHeight();
        this.fillWidth = fillWidth;
        this.color = color;
        this.text = text;
        this.font = font;
        this.ascent = textMetrics.getAscent();
    }

    private static Color darkenColor(Color orig, double percent) {
        int r = (int) Math.round(orig.getRed() * percent);
        int g = (int) Math.round(orig.getGreen() * percent);
        int b = (int) Math.round(orig.getBlue() * percent);
        return new Color(r,g,b);
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {
        Color empty = darkenColor(color, 0.3);

        graphics.setColor(empty);
        graphics.fillRect(x, y + 1, width, height - 2);

        graphics.setColor(color);
        graphics.fillRect(x, y + 1, fillWidth, height - 2);
        graphics.drawRect(x, y + 1, width, height - 2);

        graphics.setColor(Color.WHITE);
        graphics.setFont(font);
        graphics.drawString(text, x, y + ascent);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
}
