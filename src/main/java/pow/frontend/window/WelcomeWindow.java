package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WelcomeWindow extends AbstractWindow {

    private final String version;
    private final BufferedImage splashImage;

    public WelcomeWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        version = getVersion();
        splashImage = ImageController.getSplashScreenImage();
        int width = splashImage.getWidth();
        int height = splashImage.getHeight();
        this.dim = WindowDim.center(width + 2*Style.SMALL_MARGIN, height + 2* Style.SMALL_MARGIN,
                this.frontend.width, this.frontend.height);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.setState(Frontend.State.OPEN_GAME);
    }

    @Override
    public void drawContents(Graphics graphics) {

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);
        graphics.drawImage(splashImage, Style.SMALL_MARGIN, Style.SMALL_MARGIN, null);

        graphics.setFont(Style.getBigFont());
        graphics.setColor(Color.WHITE);
        graphics.drawString(version, 30, 120);
        graphics.drawString("Press any key to begin.", 30, 150);
    }

    private String getVersion() {
        Properties prop = new Properties();
        try (InputStream input = this.getClass().getResourceAsStream("/application.properties")) {
            prop.load(input);
            return prop.getProperty("application.version");
        } catch (IOException e) {
            return e.toString();
        }
    }
}
