package pow.frontend;

import pow.backend.GameBackend;
import pow.backend.event.GameEvent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Stack;

// TODO: make interface for this
public class Frontend {

    private Stack<AbstractWindow> windows;

    private int width;
    private int height;
    private GameWindow gameWindow;
    private WelcomeWindow welcomeWindow;
    private WinWindow winWindow;
    private LoseWindow loseWindow;
    private GameBackend gameBackend;

    public Frontend(int width, int height) {
        this.width = width;
        this.height = height;
        gameBackend = new GameBackend();
        gameWindow = new GameWindow(5, 5, 600, 600, true, gameBackend, this);
        welcomeWindow = new WelcomeWindow(5, 5, 600, 600, true, gameBackend, this);
        winWindow = new WinWindow(15, 100, 580, 200, true, gameBackend, this);
        loseWindow = new LoseWindow(15, 100, 480, 200, true, gameBackend, this);

        windows = new Stack<>();
        open(gameWindow);
        open(welcomeWindow);
    }

    public void open(AbstractWindow window) {
        windows.push(window);
    }

    public void close() {
        windows.pop();
    }

    public void processKey(KeyEvent e) {
        if (!windows.isEmpty()) {
            windows.peek().processKey(e);
        }

        // TODO: should this be moved inside processKey?
        // now.. process any stuff in the gameBackend
        List<GameEvent> events = gameBackend.processCommand();

        for (GameEvent event : events) {
            switch (event) {
                case WON_GAME: open(this.winWindow); break;
                case LOST_GAME: open(this.loseWindow); break;
            }
        }
    }

    public void draw(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        for (AbstractWindow w : windows) {
            w.draw(graphics);
        }
    }
}
