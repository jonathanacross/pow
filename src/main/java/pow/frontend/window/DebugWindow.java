package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class DebugWindow extends AbstractWindow {

    public DebugWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        int squareSize = 12;
        Font f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);

        int numMessages = 50;
        List<String> messages = backend.getGameState().debug;
        int row = 0;
        int startIdx = Math.max(0, messages.size() - numMessages);
        for (int idx = startIdx; idx < messages.size(); idx++) {
            graphics.drawString(messages.get(idx), 10, row*squareSize + 20);
            row++;
        }
    }
}
