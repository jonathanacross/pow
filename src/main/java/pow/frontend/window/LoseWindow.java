package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class LoseWindow extends AbstractWindow {

    private final BufferedImage splashImage;

    public LoseWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        splashImage = ImageController.getGameOverImage();
        int width = splashImage.getWidth();
        int height = splashImage.getHeight();
        this.dim = WindowDim.center(width + 2* Style.SMALL_MARGIN, height + 2*Style.SMALL_MARGIN,
                this.frontend.width, this.frontend.height);
    }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);
        if (input == KeyInput.OKAY) {
            frontend.setState(Frontend.State.OPEN_GAME);
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);
        graphics.drawImage(splashImage, Style.SMALL_MARGIN, Style.SMALL_MARGIN, null);

        graphics.setFont(Style.getBigFont());
        graphics.setColor(Color.WHITE);
        graphics.drawString("You died.", 50, 45);
        graphics.drawString("Press [enter] to continue.", 50, dim.height - 35);
    }
}
