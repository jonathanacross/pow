package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
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

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        ImageController.drawTile(graphics, imageName, Style.MARGIN, Style.MARGIN, ImageController.DrawMode.NORMAL);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < messages.size(); i++) {
            graphics.drawString(messages.get(i), 2 * Style.MARGIN + Style.TILE_SIZE, Style.MARGIN + (i+1)*Style.FONT_SIZE);
        }

        graphics.drawString("> " + countString, 2*Style.MARGIN + Style.TILE_SIZE, Style.MARGIN + (messages.size() + 2)*Style.FONT_SIZE);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Press [esc] to cancel.", 2*Style.MARGIN + Style.TILE_SIZE, dim.height - Style.MARGIN);
    }
}
