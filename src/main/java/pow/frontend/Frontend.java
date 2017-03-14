package pow.frontend;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.backend.action.RestAtInn;
import pow.backend.event.GameEvent;
import pow.backend.event.GameResult;
import pow.frontend.effect.ArrowEffect;
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

    private final Stack<AbstractWindow> windows;

    public int width;
    public int height;
    private final GameWindow gameWindow;
    private final WelcomeWindow welcomeWindow;
    private final WinWindow winWindow;
    private final LoseWindow loseWindow;
    private final CreateCharWindow createCharWindow;
    private final OpenGameWindow openGameWindow;
    public final MonsterInfoWindow monsterInfoWindow;
    public final PlayerInfoWindow playerInfoWindow;
    private final LogWindow logWindow;
    private final StatusWindow statusWindow;
    private final MapWindow mapWindow;
    private final MessageWindow messageWindow;
    public final HelpWindow helpWindow;

    private final GameBackend gameBackend;
    private final Queue<KeyEvent> keyEvents;

    private final List<Effect> effects;
    private boolean dirty;  // need to redraw
    public final Stack<String> messages;  // short messages/help suggestions

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
        welcomeWindow = new WelcomeWindow(WindowDim.center(600, 600, this.width, this.height), true, gameBackend, this);
        winWindow = new WinWindow(WindowDim.center(580, 200, this.width, this.height), true, gameBackend, this);
        loseWindow = new LoseWindow(WindowDim.center(480, 200, this.width, this.height), true, gameBackend, this);
        createCharWindow = new CreateCharWindow(WindowDim.center(480, 200, this.width, this.height), gameBackend, this);
        openGameWindow = new OpenGameWindow(WindowDim.center(380, 300, this.width, this.height), true, gameBackend, this);
        // main game
        statusWindow = new StatusWindow(new WindowDim(5, 5, 200, 707), true, gameBackend, this);
        gameWindow = new GameWindow(new WindowDim(210, 5, 672, 672), true, gameBackend, this);
        mapWindow = new MapWindow(new WindowDim(887, 5, 300, 250), true, gameBackend, this);
        logWindow = new LogWindow(new WindowDim(887, 260, 300, 452), true, gameBackend, this);
        messageWindow = new MessageWindow(new WindowDim(210, 682, 672, 30), true, gameBackend, this);
        // popups in main game
        monsterInfoWindow = new MonsterInfoWindow(new WindowDim(887, 260,300,350), false, gameBackend, this);
        playerInfoWindow = new PlayerInfoWindow(new WindowDim(100, 100,625,456), true, gameBackend, this);
        helpWindow = new HelpWindow(new WindowDim(210, 5,672,672), true, gameBackend, this);

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

    private void processKey(KeyEvent e) {
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
                case ARROW: this.effects.add(new ArrowEffect(event.actor, event.point)); break;
                case IN_STORE: processShopEntry(); break;
            }
        }
    }

    private void processShopEntry() {
        ShopData shopData = gameBackend.getGameState().getCurrentMap().shopData;
        WindowDim dim;
        List<ShopData.ShopEntry> entries;
        switch (shopData.state) {
            case INN:
                dim = WindowDim.center(600, 120, width, height);
                int cost = shopData.innCost;
                open(new ConfirmWindow(dim, true, gameBackend, this,
                        "Do you want to rest at the inn? It costs " + cost + " gold.",
                        "Rest", "Cancel",
                        () -> gameBackend.tellPlayer(new RestAtInn())));
                break;
            case WEAPON_SHOP:
                dim = WindowDim.center(400, 500, width, height);
                entries = shopData.weaponItems;
                open(new ShopWindow(dim, true, gameBackend, this, entries));
                break;
            case MAGIC_SHOP:
                dim = WindowDim.center(400, 500, width, height);
                entries = shopData.magicItems;
                open(new ShopWindow(dim, true, gameBackend, this, entries));
                break;
            default:
                System.out.println("entered a shop of type " + shopData.state);
        }
    }

    private static final Color BACKGROUND_COLOR = Color.BLACK;

    public void draw(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        graphics2D.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

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
