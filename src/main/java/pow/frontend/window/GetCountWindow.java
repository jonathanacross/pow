package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.util.MathUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

public class GetCountWindow extends AbstractWindow {

    private String countString;
    private String message;
    private int maxNum;
    private IntConsumer callback;

    public GetCountWindow(WindowDim dim, GameBackend backend, Frontend frontend, String message, int maxNum, IntConsumer callback) {
        super(dim, true, backend, frontend);
        this.countString = "";
        this.message = message;
        this.maxNum = maxNum;
        this.callback = callback;
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            frontend.close();
            return;
        }
        if (keyCode == KeyEvent.VK_ENTER && !countString.isEmpty()) {
            int count = Integer.parseInt(countString);
            if (count == 0) {
                frontend.close();  // the the player escape out
                return;
            }
            count = MathUtils.clamp(count, 1, maxNum);
            frontend.close();
            callback.accept(count);
            return;
        }

        char c = e.getKeyChar();
        if (Character.isDigit(c)) {
            countString = countString + c;
            frontend.setDirty(true);
        } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
            if (countString.length() > 0) {
                countString = countString.substring(0, countString.length() - 1);
                frontend.setDirty(true);
            }
        }
    }

    final private int MARGIN = 20;
    final private int FONT_SIZE = 12;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString(message, MARGIN, MARGIN + FONT_SIZE);
        graphics.drawString("(max " + maxNum + ")", MARGIN, MARGIN + 2*FONT_SIZE);

        graphics.drawString("> " + countString, MARGIN, MARGIN + 4*FONT_SIZE);
    }
}
