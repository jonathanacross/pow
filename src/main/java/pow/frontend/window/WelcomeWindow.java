package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WelcomeWindow extends AbstractWindow {

    private final String version;

    public WelcomeWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        version = getVersion();
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.setState(Frontend.State.OPEN_GAME);
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        int squareSize = 18;
        Font f = new Font("Courier", Font.PLAIN, 2*squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.YELLOW);
        graphics.drawString("Pearls of Wisdom", 30, 50);

        f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);

        graphics.drawString(version, 30, 100);
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
