package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;

import java.awt.*;
import java.awt.event.KeyEvent;

public class StatusWindow extends AbstractWindow {

    public StatusWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
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
        String name = backend.getGameState().name;
        graphics.drawString(name, 10, 20);
        graphics.drawString("HP:", 10, 20 + squareSize);
        graphics.drawString("MP:", 10, 20 + 2*squareSize);
    }
}
