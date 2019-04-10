package pow.frontend.widget;

import pow.frontend.utils.ImageUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<TextAttribute, Integer> underlined = new HashMap<>();
        underlined.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

        // vertically center
        int yOffset = y + ascent;
        for (String line : lines) {
            switch (state) {
                case NORMAL: graphics.setColor(Color.WHITE); graphics.setFont(font); break;
                case DISABLED: graphics.setColor(Color.GRAY); graphics.setFont(font); break;
                case SELECTED: graphics.setColor(Color.YELLOW); graphics.setFont(font); break;
                case HEADER1: graphics.setColor(Color.WHITE);
                    graphics.setFont(font.deriveFont(Font.BOLD, (float) (font.getSize()*1.3)).deriveFont(underlined));
                    break;
                case HEADER2:
                    graphics.setColor(Color.WHITE); graphics.setFont(font.deriveFont(Font.BOLD).deriveFont(underlined));
                    break;
                case ERROR: graphics.setColor(Color.RED); graphics.setFont(font); break;
            }
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
