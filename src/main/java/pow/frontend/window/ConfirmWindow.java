package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ConfirmWindow extends AbstractWindow {

    private final String message;
    private final String okayText;
    private final String cancelText;
    private final Runnable action;

    public ConfirmWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend,
                         String message, String okayText, String cancelText, Runnable action) {
        super(dim, visible, backend, frontend);
        this.message = message;
        this.okayText = okayText;
        this.cancelText = cancelText;
        this.action = action;
    }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case OKAY:
                frontend.close();
                action.run();
                break;
            case CANCEL:
                frontend.close();
                break;
            default:
                break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        graphics.drawString(message, Style.MARGIN, Style.MARGIN + Style.getFontSize());
        graphics.drawString("Esc:    " + cancelText, 2 * Style.MARGIN, Style.MARGIN + 3*Style.getFontSize());
        graphics.drawString("Enter:  " + okayText, 2 * Style.MARGIN, Style.MARGIN + 4*Style.getFontSize());
    }
}
