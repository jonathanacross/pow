package game.frontend;

import game.GameBackend;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class WelcomeWindow extends AbstractWindow {

    public WelcomeWindow(int x, int y, int width, int height, boolean visible, GameBackend backend) {
        super(x, y, width, height, visible, backend);
    }

    @Override
    public void processKey(KeyEvent e) {

    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);

        int squareSize = 18;
        Font f = new Font("Courier New", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.drawString("Pearls of Wisdom.", 30, 30);
        graphics.drawString("Press any key to begin.", 30, 100);
    }
}
