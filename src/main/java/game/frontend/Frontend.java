package game.frontend;

import game.GameBackend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Stack;

// TODO: make interface for this
public class Frontend {

    private Stack<AbstractWindow> windows;

    private int width;
    private int height;
    private GameWindow gameWindow;
    private WelcomeWindow welcomeWindow;
    private GameBackend gameBackend;

    public Frontend(int width, int height) {
        this.width = width;
        this.height = height;
        gameBackend = new GameBackend();
        gameWindow = new GameWindow(5, 5, 600, 600, true, gameBackend, this);
        welcomeWindow = new WelcomeWindow(5, 5, 600, 600, true, gameBackend, this);

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
        gameBackend.processCommand();

        // handle different game states..?
        //switch (gameBackend.getGameState() == GameState.MetaGameState.WELCOME)
    }

    public void draw(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        for (AbstractWindow w : windows) {
            w.draw(graphics);
        }
    }
}
