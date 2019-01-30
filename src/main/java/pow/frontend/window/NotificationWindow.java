package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;

import java.awt.*;
import java.awt.event.KeyEvent;

public class NotificationWindow extends AbstractWindow {
    private final String message;

    public NotificationWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend,
                         String message) {
        super(dim, visible, backend, frontend);
        this.message = message;
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        graphics.drawString(message, Style.SMALL_MARGIN, Style.SMALL_MARGIN + Style.FONT_SIZE);
    }
}
