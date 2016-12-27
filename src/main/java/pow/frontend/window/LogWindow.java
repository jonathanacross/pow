package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.frontend.Frontend;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;

public class LogWindow extends AbstractWindow {

    public LogWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        int squareSize = 14;
        Font f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);

        List<MessageLog.Message> messages = backend.getGameState().log.getLastN(20);
        int row = 0;
        for (int idx = 0; idx < messages.size(); idx++) {
            graphics.drawString(messages.get(idx).toString(), 10, row*squareSize + 20);
            row++;
        }
    }
}
