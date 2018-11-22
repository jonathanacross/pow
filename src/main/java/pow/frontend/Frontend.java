package pow.frontend;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.backend.action.ExitPortal;
import pow.backend.action.RestAtInn;
import pow.backend.action.UpgradeItem;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.gen.CharacterGenerator;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.backend.event.GameEvent;
import pow.backend.event.GameResult;
import pow.frontend.utils.SaveUtils;
import pow.frontend.window.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Queue;

// TODO: make interface for this
public class Frontend {

    private final Deque<AbstractWindow> windows;

    public int width;
    public int height;
    private final GameWindow gameWindow;
    private final WelcomeWindow welcomeWindow;
    private final WinWindow winWindow;
    private final LoseWindow loseWindow;
    public final AutoplayOptionWindow autoplayOptionWindow;
    private final OpenGameWindow openGameWindow;
    public final MonsterInfoWindow monsterInfoWindow;
    public final PlayerInfoWindow playerInfoWindow;
    public final WorldMapWindow worldMapWindow;
    private final LogWindow logWindow;
    private final StatusWindow statusWindow;
    private final MapWindow mapWindow;
    private final MessageWindow messageWindow;
    public final HelpWindow helpWindow;

    private final GameBackend gameBackend;
    private final Queue<KeyEvent> keyEvents;

    private final List<DungeonEffect> effects;
    private boolean dirty;  // need to redraw
    public final Deque<String> messages;  // short messages/help suggestions

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<DungeonEffect> getEffects() {
        return effects;
    }

    public Frontend(int width, int height) {
        this.width = width;
        this.height = height;
        this.dirty = true;
        this.effects = new ArrayList<>();
        this.keyEvents = new ArrayDeque<>();
        this.messages = new ArrayDeque<>();

        gameBackend = new GameBackend();
        // dialogs
        welcomeWindow = new WelcomeWindow(WindowDim.center(600, 600, this.width, this.height), true, gameBackend, this);
        winWindow = new WinWindow(WindowDim.center(580, 200, this.width, this.height), true, gameBackend, this);
        loseWindow = new LoseWindow(WindowDim.center(480, 200, this.width, this.height), true, gameBackend, this);
        autoplayOptionWindow = new AutoplayOptionWindow(WindowDim.center(210, 140, this.width, this.height), gameBackend, this);
        openGameWindow = new OpenGameWindow(WindowDim.center(380, 300, this.width, this.height), true, gameBackend, this);
        // main game
        statusWindow = new StatusWindow(new WindowDim(5, 5, 200, 707), true, gameBackend, this);
        gameWindow = new GameWindow(new WindowDim(210, 5, 672, 672), true, gameBackend, this);
        mapWindow = new MapWindow(new WindowDim(887, 5, 300, 250), true, gameBackend, this);
        logWindow = new LogWindow(new WindowDim(887, 260, 300, 452), true, gameBackend, this);
        messageWindow = new MessageWindow(new WindowDim(210, 682, 672, 30), true, gameBackend, this);
        // popups in main game
        monsterInfoWindow = new MonsterInfoWindow(new WindowDim(887, 260,300,452), false, gameBackend, this);
        playerInfoWindow = new PlayerInfoWindow(WindowDim.center(635, 470, this.width, this.height), true, gameBackend, this);
        worldMapWindow = new WorldMapWindow(new WindowDim(210, 5,672,672), true, gameBackend, this);
        helpWindow = new HelpWindow(new WindowDim(210, 5,672,672), true, gameBackend, this);

        windows = new ArrayDeque<>();
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
                createMainCharacter();
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

        // Remove the current effect from our list to draw so that the
        // next one will display.
        if (! effects.isEmpty()) {
            effects.remove(0);
            dirty = true;
            return;
        }

        // finished all visual effects; now process future actions
        KeyEvent keyEvent = keyEvents.poll();
        if (keyEvent != null) {
            processKey(keyEvent);
        }

        GameResult result = gameBackend.update();
        if (!result.events.isEmpty()) {
            dirty = true;
        }
        for (GameEvent event : result.events) {
            switch (event.eventType) {
                case WON_GAME: open(this.winWindow); break;
                case LOST_GAME: open(this.loseWindow); break;
                case EFFECT: this.effects.add(event.effect); break;
                case IN_STORE: processShopEntry(); break;
                case IN_PORTAL: choosePortal(); break;
                case GOT_PET: choosePet(); break;
                default: break;
            }
        }
    }

    public void addKeyEvent(KeyEvent e) {
        this.keyEvents.add(e);
    }

    private void processKey(KeyEvent e) {
        if (!windows.isEmpty()) {
            windows.peek().processKey(e);
        }
    }

    private void startNewGame(SelectCharWindow.NamedCharData data) {
        Player player = CharacterGenerator.getPlayer(data.name, data.characterData.id);
        gameBackend.newGame(player);
        this.setState(Frontend.State.GAME);
    }

    // Start the game if
    // (1) it's a new character name, or
    // (2) an existing character name and user has confirmed they want to overwrite.
    private void tryToStartNewGame(SelectCharWindow.NamedCharData data) {
        // see if there's already a character with this name
        List<File> existingFiles = SaveUtils.findSaveFiles();
        boolean alreadyExists = false;
        for (File f : existingFiles) {
            if (f.getName().equals(data.name)) {
                alreadyExists = true;
            }
        }

        if (alreadyExists) {
            WindowDim dim = WindowDim.center(600, 120, width, height);
            open(new ConfirmWindow(dim, true, gameBackend, this,
                    "The character '" + data.name + "' already exists.  Do you want to overwrite it?",
                    "Overwrite", "Cancel",
                    () -> startNewGame(data)));
        } else {
            startNewGame(data);
        }
    }

    private void createMainCharacter() {
        gameBackend.setGameInProgress(false);
        SelectCharWindow createCharWindow = new SelectCharWindow(
                WindowDim.center(480, 240, this.width, this.height),
                gameBackend, this,
                Arrays.asList("Select your character:"), false,
                this::tryToStartNewGame, () -> setState(Frontend.State.OPEN_GAME));
        open(createCharWindow);
    }

    private void choosePet() {
        SelectCharWindow createCharWindow = new SelectCharWindow(
                WindowDim.center(480, 260, this.width, this.height),
                gameBackend, this,
                Arrays.asList("Congratulations, you got a pet!", "Select your character:"), true,
                (data) -> {
                    Player pet = CharacterGenerator.getPlayer(data.name, data.characterData.id);
                    gameBackend.setPet(pet);
                    close();
                },
                () -> {});
        open(createCharWindow);
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
                        () -> gameBackend.tellSelectedActor(new RestAtInn())));
                break;
            case WEAPON_SHOP:
                dim = WindowDim.center(450, 500, width, height);
                entries = shopData.weaponItems;
                open(new ShopWindow(dim, true, gameBackend, this, entries));
                break;
            case MAGIC_SHOP:
                dim = WindowDim.center(450, 500, width, height);
                entries = shopData.magicItems;
                open(new ShopWindow(dim, true, gameBackend, this, entries));
                break;
            case JEWELER_SHOP:
                dim = WindowDim.center(650, 550, width, height);
                open(new JewelerShopWindow(dim, true, gameBackend, this,
                        (UpgradeItem.UpgradeInfo i) -> gameBackend.tellSelectedActor(new UpgradeItem(i))));
                break;
            default:
                System.out.println("entered a shop of type " + shopData.state);
        }
    }

    private void choosePortal() {
        Map<String, MapPoint.PortalStatus> portals = gameBackend.getGameState().world.topologySummary.getPortals();
        List<String> openPortalAreas = new ArrayList<>();
        for (Map.Entry<String, MapPoint.PortalStatus> entry : portals.entrySet()) {
            if (entry.getValue() == MapPoint.PortalStatus.OPEN) {
                openPortalAreas.add(entry.getKey());
            }
        }
        Collections.sort(openPortalAreas);

        open(new PortalChoiceWindow(300, 150,
                gameBackend, this,
                "Which area do you wish to go to?",
                openPortalAreas,
                (String areaId) -> gameBackend.tellSelectedActor(new ExitPortal(gameBackend.getGameState().party.player, areaId))
        ));
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
        // draw back to front
        Iterator<AbstractWindow> i = windows.descendingIterator();
        while (i.hasNext()) {
            i.next().draw(graphics);
        }
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
