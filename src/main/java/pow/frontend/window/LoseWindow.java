package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class LoseWindow extends AbstractWindow {

    public LoseWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
        backend.newGame();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        int squareSize = 18;
        Font f = new Font("Courier", Font.PLAIN, 2*squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.YELLOW);
        graphics.drawString("You died. :(", 30, 50);

        f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Press any key to continue.", 30, 150);
    }
}
