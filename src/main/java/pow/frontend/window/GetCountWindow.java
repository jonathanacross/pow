package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.util.MathUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;
import java.util.List;

public class GetCountWindow extends AbstractWindow {

    private String countString;
    private final String imageName;
    private final List<String> messages;
    private final int maxNum;
    private final IntConsumer callback;

    public GetCountWindow(WindowDim dim, GameBackend backend, Frontend frontend, String imageName,
                          List<String> messages, int maxNum, IntConsumer callback) {
        super(dim, true, backend, frontend);
        this.countString = "";
        this.imageName = imageName;
        this.messages = messages;
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
                frontend.close();  // let the player escape out
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
    final private int TILE_SIZE = 32;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        ImageController.drawTile(graphics, imageName, MARGIN, MARGIN, ImageController.DrawMode.NORMAL);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < messages.size(); i++) {
            graphics.drawString(messages.get(i), 2 * MARGIN + TILE_SIZE, MARGIN + (i+1)*FONT_SIZE);
        }

        graphics.drawString("> " + countString, 2*MARGIN + TILE_SIZE, MARGIN + (messages.size() + 2)*FONT_SIZE);
    }
}
