package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class MessageWindow extends AbstractWindow {

    public MessageWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.setState(Frontend.State.OPEN_GAME);
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        int fontSize = 12;
        Font f = new Font("Courier", Font.PLAIN, fontSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);

        if (frontend.lookMessage != null) {
            graphics.drawString(frontend.lookMessage, 20, 12);
        }
        if (!frontend.messages.isEmpty()) {
            graphics.drawString(frontend.messages.peek(), 20, 26);
        }
    }
}
