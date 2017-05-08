package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class WinWindow extends AbstractWindow {

    public WinWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);
        if (input == KeyInput.OKAY) {
            frontend.close();
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        int squareSize = 18;
        Font f = new Font("Courier", Font.PLAIN, 2*squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.YELLOW);
        graphics.drawString("Congratulations, you won!", 20, 50);

        f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Press enter to continue.", 20, 150);
    }
}
