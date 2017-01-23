package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.utils.HelpController;

import java.awt.*;
import java.awt.event.KeyEvent;

public class HelpWindow extends AbstractWindow {

    public HelpWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
    }

    private static final int TEXT_SIZE = 14;
    private static final int MARGIN = 10;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        Font font = new Font("Courier", Font.PLAIN, TEXT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        int y = TEXT_SIZE + MARGIN;
        for (String line: HelpController.getHelpText()) {
            graphics.drawString(line, MARGIN, y);
            y += TEXT_SIZE;
        }
    }
}
