package pow.frontend.widget;

import pow.frontend.utils.ImageUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TextBox implements Widget {

    // Used to get font metrics to compute text heights/widths
    private static Graphics fakeGraphics;
    static {
        Image dummyImage = new BufferedImage(10, 10,  BufferedImage.TYPE_INT_ARGB);
        fakeGraphics = dummyImage.getGraphics();
    }

    private List<String> lines;
    private State state;
    private Font font;
    private int width;
    private int height;
    private int lineHeight;
    private int ascent;

    // force width by wrapping text
    public TextBox(List<String> lines, State state, Font font, int width) {
        this.state = state;
        this.font = font;

        FontMetrics textMetrics = fakeGraphics.getFontMetrics(font);
        this.lineHeight = textMetrics.getHeight();
        this.ascent = textMetrics.getAscent();
        this.width = width;
        this.lines = new ArrayList<>();
        for (String line : lines) {
            List<String> wrappedLines = ImageUtils.wrapText(line, textMetrics, width);
            this.lines.addAll(wrappedLines);

        }
        this.height = textMetrics.getHeight() * this.lines.size();
    }

    // compute width based on given text
    public TextBox(List<String> lines, State state, Font font) {
        this.lines = lines;
        this.state = state;
        this.font = font;

        FontMetrics textMetrics = fakeGraphics.getFontMetrics(font);
        this.lineHeight = textMetrics.getHeight();
        this.ascent = textMetrics.getAscent();
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, textMetrics.stringWidth(line));
        }
        this.width = maxWidth;
        this.height = textMetrics.getHeight() * lines.size();
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {
        // vertically center
        int yOffset = y + ascent;
        for (String line : lines) {
            switch (state) {
                case NORMAL: graphics.setColor(Color.WHITE); break;
                case DISABLED: graphics.setColor(Color.GRAY); break;
                case SELECTED: graphics.setColor(Color.YELLOW); break;
            }
            graphics.setFont(font);
            graphics.drawString(line, x, yOffset);
            yOffset += lineHeight;
        }
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
