package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class CreateCharWindow extends AbstractWindow {

    private String name;

    public CreateCharWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
        resetName();
    }

    public void resetName() {
        name = "";
    }

    @Override
    public void processKey(KeyEvent e) {
        char c = e.getKeyChar();
        if (Character.isLetterOrDigit(c) || c == ' ') {
            name = name + c;
            frontend.setDirty(true);
        }
        else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (name.length() > 0) {
                name = name.substring(0, name.length() - 1);
                frontend.setDirty(true);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (! name.trim().equals("")) {
                backend.newGame(name);
                frontend.setState(Frontend.State.GAME);
            } else {
                name = name.trim();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            frontend.setState(Frontend.State.OPEN_GAME);

        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        int squareSize = 18;
        Font f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Enter the name of your character.", 30, 50);

        graphics.drawString(name, 30, 70);
    }
}
