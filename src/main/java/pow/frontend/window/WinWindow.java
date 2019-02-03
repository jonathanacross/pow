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

public class WinWindow extends AbstractWindow {

    private final BufferedImage splashImage;

    public WinWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        splashImage = ImageController.getPearlImage();
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
        graphics.drawString("Congratulations, you have won!", 20, 50);
        graphics.drawString("You have returned all the pearls to the temple.", 20, 68);

        graphics.drawString("Press [enter] to continue playing.", 20, dim.height - 35);
    }
}
