package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class MessageWindow extends AbstractWindow {

    public MessageWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.setState(Frontend.State.OPEN_GAME);
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        int fontSize = 12;
        Font f = new Font("Courier", Font.PLAIN, fontSize);
        graphics.setFont(f);

        if (!frontend.messages.empty()) {
            graphics.setColor(Color.WHITE);
            graphics.drawString(frontend.messages.peek(), 20, 20);
        }
    }
}
