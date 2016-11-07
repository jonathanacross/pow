package game.frontend;

import game.GameBackend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Stack;

// TODO: rename to desktop, or windowManager?
public class Frontend extends AbstractWindow {

    private Stack<AbstractWindow> windows;

    private GameWindow gameWindow;
    private WelcomeWindow welcomeWindow;

    public Frontend(int x, int y, int width, int height, GameBackend backend) {
        super(x, y, width, height, true, backend);

        gameWindow = new GameWindow(5, 5, 600, 600, true, backend);
        welcomeWindow = new WelcomeWindow(5, 5, 600, 600, true, backend);

        windows = new Stack<>();
        open(gameWindow);
    }

    void open(AbstractWindow window) {
        windows.push(window);
    }

    void close() {
        windows.pop();
    }

    @Override
    public void processKey(KeyEvent e) {
        if (!windows.isEmpty()) {
            windows.peek().processKey(e);
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        for (AbstractWindow w : windows) {
            w.draw(graphics);
        }
    }
}
