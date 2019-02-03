package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Win2Window extends AbstractWindow {

    private final BufferedImage splashImage;

    public Win2Window(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        splashImage = ImageController.getLandscapeImage();
        int width = splashImage.getWidth();
        int height = splashImage.getHeight();
        this.dim = this.frontend.layout.center(width + 2 * Style.SMALL_MARGIN, height + 2 * Style.SMALL_MARGIN);

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
        graphics.drawImage(splashImage, Style.SMALL_MARGIN, Style.SMALL_MARGIN, null);

        graphics.setFont(Style.getBigFont());
        graphics.setColor(Color.WHITE);
        graphics.drawString("Congratulations, you have defeated Evil Incarnate!", 20, 50);
        graphics.drawString("The world is a happier place.", 30, 70);

        graphics.drawString("Press [enter] to continue.", 30, dim.height - 35);
    }
}
