package pow.frontend;

import pow.backend.GameBackend;
import pow.backend.event.GameEvent;
import pow.frontend.effect.Effect;
import pow.frontend.effect.RocketEffect;
import pow.frontend.window.AbstractWindow;
import pow.frontend.window.CreateCharWindow;
import pow.frontend.window.GameWindow;
import pow.frontend.window.LoseWindow;
import pow.frontend.window.OpenGameWindow;
import pow.frontend.window.WelcomeWindow;
import pow.frontend.window.WinWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    private CreateCharWindow createCharWindow;
    private OpenGameWindow openGameWindow;

    private GameBackend gameBackend;
    private Queue<KeyEvent> keyEvents;

    private List<Effect> effects;
    private boolean dirty;  // need to redraw

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public Frontend(int width, int height) {
        this.width = width;
        this.height = height;
        this.dirty = true;
        this.effects = new ArrayList<>();
        this.keyEvents = new LinkedList<>();

        gameBackend = new GameBackend();
        gameWindow = new GameWindow(5, 5, 600, 600, true, gameBackend, this);
        welcomeWindow = new WelcomeWindow(5, 5, 600, 600, true, gameBackend, this);
        winWindow = new WinWindow(15, 100, 580, 200, true, gameBackend, this);
        loseWindow = new LoseWindow(15, 100, 480, 200, true, gameBackend, this);
        createCharWindow = new CreateCharWindow(15, 100, 480, 200, true, gameBackend, this);
        openGameWindow = new OpenGameWindow(15, 100, 380, 300, true, gameBackend, this);

        windows = new Stack<>();
        setState(State.WELCOME);
    }

    // basic frontend states, corresponding to main windows
    public enum State {
        GAME,
        WELCOME,
        OPEN_GAME,
        CREATE_CHAR
    }

    public void setState(State state) {
        windows.clear();
        switch (state) {
            case GAME: windows.push(gameWindow); break;
            case WELCOME: windows.push(welcomeWindow); break;
            case OPEN_GAME: openGameWindow.refreshFileList(); windows.push(openGameWindow); break;
            case CREATE_CHAR: createCharWindow.resetName(); windows.push(createCharWindow); break;
        }
        dirty = true;
    }

    public void open(AbstractWindow window) {
        windows.push(window);
        dirty = true;
    }

    public void close() {
        windows.pop();
        dirty = true;
    }

    public void update() {
        while (! effects.isEmpty()) {
            Effect effect = effects.get(0);
            dirty = true;
            if (effect.update()) {
                // continue animation of current effect
                return;
            } else {
                effects.remove(0);
            }
        }

        // finished all visual effects; now process future actions
        KeyEvent keyEvent = keyEvents.poll();
        if (keyEvent != null) {
            processKey(keyEvent);
        }
    }

    public void addKeyEvent(KeyEvent e) {
        this.keyEvents.add(e);
    }

    public void processKey(KeyEvent e) {
        if (!windows.isEmpty()) {
            windows.peek().processKey(e);
        }

        List<GameEvent> events = gameBackend.processCommand();
        if (! events.isEmpty()) {
            dirty = true;
        }
        for (GameEvent event : events) {
            switch (event) {
                case WON_GAME: open(this.winWindow); break;
                case LOST_GAME: open(this.loseWindow); break;
                case ROCKET: this.effects.add(new RocketEffect(this.gameBackend)); break;
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

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
