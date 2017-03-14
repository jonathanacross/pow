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
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        int fontSize = 14;
        int margin = 30;

        Font font = new Font("Courier", Font.PLAIN, fontSize);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(message, margin, margin + fontSize);
        graphics.drawString("Esc:    " + cancelText, 2 * margin, margin + 3*fontSize);
        graphics.drawString("Enter:  " + okayText, 2 * margin, margin + 4*fontSize);
    }
}
