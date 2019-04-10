package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class LogWindow extends AbstractWindow {

    public LogWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    private static final int MESSAGE_SEP = 3;
    private static final int TEXT_INDENT = 16;

    private static final Color LAVENDER = new Color(192, 128, 255);
    private static final Color ORANGE = new Color(255, 172, 0);
    private static final Color LIGHT_BLUE = new Color(64, 128,255);

    private static class LinePos {
        public final String line;
        public final Color color;
        public final int x;
        public final int y;

        public LinePos(String line, Color color, int x, int y) {
            this.line = line;
            this.color = color;
            this.x = x;
            this.y = y;
        }
    }

    // See where to draw each line naively.
    private List<LinePos> getLinePositions(List<MessageLog.Message> messages,
                                           FontMetrics textMetrics,
                                           int textWidth) {
        List<LinePos> linePositions = new ArrayList<>();
        int lineHeight = textMetrics.getHeight();
        int ascent = textMetrics.getAscent();

        int y = ascent + Style.SMALL_MARGIN;
        for (MessageLog.Message message : messages) {
            boolean firstLine = true;
            List<String> messageLines = ImageUtils.wrapText("> " + message.toString(),
                    textMetrics, textWidth, textWidth - TEXT_INDENT);
            for (String line : messageLines) {
                linePositions.add(new LinePos(line, getMessageColor(message),
                        Style.SMALL_MARGIN + (firstLine ? 0 : TEXT_INDENT), y));
                firstLine = false;
                y += lineHeight;
            }
            y += MESSAGE_SEP;
        }

        return linePositions;
    }

    private Color getMessageColor(MessageLog.Message message) {
        switch (message.type) {
            case GENERAL: return Color.WHITE;
            case GAME_EVENT: return LAVENDER;
            case COMBAT_GOOD: return Color.GREEN;
            case COMBAT_NEUTRAL: return Color.YELLOW;
            case COMBAT_BAD: return Color.RED;
            case USER_ERROR: return ORANGE;
            case STATUS: return Color.CYAN;
            case DEBUG: return LIGHT_BLUE;
        }
        // Shouldn't get here.
        return Color.WHITE;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = Style.getDefaultFont();
        graphics.setFont(font);

        int textWidth = dim.width - 2*Style.SMALL_MARGIN;

        FontMetrics textMetrics = graphics.getFontMetrics(font);

        List<MessageLog.Message> messages = backend.getGameState().log.getLastN(30);
        List<LinePos> lines = getLinePositions(messages, textMetrics, textWidth);

        // Shift lines up so that last line is positioned at bottom of window.
        int deltaHeight = (dim.height - Style.SMALL_MARGIN) - lines.get(lines.size() - 1).y;
        for (LinePos line: lines) {
            graphics.setColor(line.color);
            graphics.drawString(line.line, line.x, line.y + deltaHeight);
        }

    }
}
