package pow.frontend;

import pow.backend.GameBackend;
import pow.backend.event.GameEvent;
import pow.backend.event.GameResult;
import pow.frontend.effect.Effect;
import pow.frontend.effect.RocketEffect;
import pow.frontend.window.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    public MonsterInfoWindow monsterInfoWindow;
    public PlayerInfoWindow playerInfoWindow;
    private LogWindow logWindow;
    private StatusWindow statusWindow;
    private MapWindow mapWindow;
    private MessageWindow messageWindow;

    private GameBackend gameBackend;
    private Queue<KeyEvent> keyEvents;

    private List<Effect> effects;
    private boolean dirty;  // need to redraw
    public Stack<String> messages;  // short messages/help suggestions

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
        this.messages = new Stack<>();

        gameBackend = new GameBackend();
        // dialogs
        welcomeWindow = new WelcomeWindow(50, 50, 600, 600, true, gameBackend, this);
        winWindow = new WinWindow(15, 100, 580, 200, true, gameBackend, this);
        loseWindow = new LoseWindow(15, 100, 480, 200, true, gameBackend, this);
        createCharWindow = new CreateCharWindow(15, 100, 480, 200, true, gameBackend, this);
        openGameWindow = new OpenGameWindow(15, 100, 380, 300, true, gameBackend, this);
        // main game
        statusWindow = new StatusWindow(5, 5, 200, 707, true, gameBackend, this);
        gameWindow = new GameWindow(210, 5, 672, 672, true, gameBackend, this);
        mapWindow = new MapWindow(887, 5, 300, 250, true, gameBackend, this);
        logWindow = new LogWindow(887, 260, 300, 452, true, gameBackend, this);
        messageWindow = new MessageWindow(210, 682, 672, 30, true, gameBackend, this);
        // popups in main game
        monsterInfoWindow = new MonsterInfoWindow(887, 260,300,180, false, gameBackend, this);
        playerInfoWindow = new PlayerInfoWindow(100, 100,300,300, true, gameBackend, this);

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
            case GAME:
                windows.push(statusWindow);
                windows.push(logWindow);
                windows.push(messageWindow);
                windows.push(mapWindow);
                windows.push(monsterInfoWindow);
                windows.push(gameWindow);
                gameBackend.setGameInProgress(true);
                break;
            case WELCOME:
                windows.push(welcomeWindow);
                gameBackend.setGameInProgress(false);
                break;
            case OPEN_GAME:
                openGameWindow.refreshFileList();
                windows.push(openGameWindow);
                gameBackend.setGameInProgress(false);
                break;
            case CREATE_CHAR:
                createCharWindow.resetName();
                windows.push(createCharWindow);
                gameBackend.setGameInProgress(false);
                break;
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

        GameResult result = gameBackend.update();
        if (!result.events.isEmpty()) {
            dirty = true;
        }
        for (GameEvent event : result.events) {
            switch (event.eventType) {
                case WON_GAME: open(this.winWindow); break;
                case LOST_GAME: open(this.loseWindow); break;
                case ROCKET: this.effects.add(new RocketEffect(event.actor)); break;
            }
        }
    }

    private static final Color BACKGROUND_COLOR = Color.BLACK;

    public void draw(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        graphics.setColor(BACKGROUND_COLOR);
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
