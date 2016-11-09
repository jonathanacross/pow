package game.frontend;

import game.GameBackend;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class WinWindow extends AbstractWindow {

    public WinWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        int squareSize = 18;
        Font f = new Font("Courier", Font.PLAIN, 2*squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.YELLOW);
        graphics.drawString("Congratulations, you won!", 20, 50);

        f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Press any key to continue.", 20, 150);
    }
}
