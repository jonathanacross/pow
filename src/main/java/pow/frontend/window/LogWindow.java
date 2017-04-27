package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.frontend.Frontend;
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

    private static final int MARGIN = 10;
    private static final int FONT_SIZE = 14;
    private static final int MESSAGE_SEP = 3;
    private static final int TEXT_INDENT = 16;

    private static class LinePos {
        public final String line;
        public final int x;
        public final int y;

        public LinePos(String line, int x, int y) {
            this.line = line;
            this.x = x;
            this.y = y;
        }
    }

    // See where to draw each line naively.
    private List<LinePos> getLinePositions(List<MessageLog.Message> messages,
                                           FontMetrics textMetrics,
                                           int textWidth) {
        List<LinePos> linePositions = new ArrayList<>();

        int y = FONT_SIZE + MARGIN;
        for (MessageLog.Message message : messages) {
            boolean firstLine = true;
            List<String> messageLines = ImageUtils.wrapText("> " + message.toString(),
                    textMetrics, textWidth, textWidth - TEXT_INDENT);
            for (String line : messageLines) {
                linePositions.add(new LinePos(line, MARGIN + (firstLine ? 0 : TEXT_INDENT), y));
                firstLine = false;
                y += FONT_SIZE;
            }
            y += MESSAGE_SEP;
        }

        return linePositions;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);

        int textWidth = dim.width - 2*MARGIN;

        graphics.setFont(font);
        FontMetrics textMetrics = graphics.getFontMetrics(font);

        graphics.setColor(Color.WHITE);

        List<MessageLog.Message> messages = backend.getGameState().log.getLastN(30);
        List<LinePos> lines = getLinePositions(messages, textMetrics, textWidth);

        // Shift lines up so that last line is positioned at bottom of window.
        int deltaHeight = (dim.height - MARGIN) - lines.get(lines.size() - 1).y;
        for (LinePos line: lines) {
            graphics.drawString(line.line, line.x, line.y + deltaHeight);
        }

    }
}
