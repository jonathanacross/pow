package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.widget.State;
import pow.frontend.widget.TextBox;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class NotificationWindow extends AbstractWindow {
    private final TextBox textCell;

    public NotificationWindow(boolean visible, GameBackend backend, Frontend frontend,
                         String message) {
        super(new WindowDim(0, 0, 0, 0), visible, backend, frontend);
        this.textCell = new TextBox(Arrays.asList(message), State.NORMAL, Style.getDefaultFont());
        this.resize(frontend.layout.center(textCell.getWidth() + 2*Style.MARGIN,
                textCell.getHeight() + 2* Style.MARGIN));
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        textCell.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
