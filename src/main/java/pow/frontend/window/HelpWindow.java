package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.HelpController;

import java.awt.*;
import java.awt.event.KeyEvent;

public class HelpWindow extends AbstractWindow {

    public HelpWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getLargeFont());
        graphics.setColor(Color.WHITE);

        int y = Style.LARGE_FONT_SIZE + Style.MARGIN;
        for (String line: HelpController.getHelpText()) {
            graphics.drawString(line, Style.MARGIN, y);
            y += Style.LARGE_FONT_SIZE;
        }
    }
}
