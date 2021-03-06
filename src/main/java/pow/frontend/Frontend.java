package pow.frontend;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.backend.action.ExitPortal;
import pow.backend.action.RestAtInn;
import pow.backend.action.UpgradeItem;
import pow.backend.actors.Player;
import pow.backend.dungeon.gen.CharacterGenerator;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.backend.event.GameEvent;
import pow.backend.event.GameResult;
import pow.frontend.utils.SaveUtils;
import pow.frontend.window.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class Frontend {

    private final Deque<AbstractWindow> windows;
    public final WindowLayout layout;

    public int width;
    public int height;
    private final GameWindow gameWindow;
    private final WelcomeWindow welcomeWindow;
    private final WinWindow winWindow;
    private final Win2Window win2Window;
    private final LoseWindow loseWindow;
    private final OpenGameWindow openGameWindow;
    public final MonsterInfoWindow monsterInfoWindow;
    public final WorldMapWindow worldMapWindow;
    private final LogWindow logWindow;
    private final StatusWindow statusWindow;
    private final MapWindow mapWindow;
    public final HelpWindow helpWindow;

    private final GameBackend gameBackend;
    private final Queue<KeyEvent> keyEvents;

    private boolean dirty;  // need to redraw
    private final List<GameEvent> events;

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public Frontend(int width, int height) {
        this.width = width;
        this.height = height;
        this.dirty = true;
        this.keyEvents = new ArrayDeque<>();
        this.events = new ArrayList<>();

        this.layout = new WindowLayout(width, height);

        gameBackend = new GameBackend();
        // dialogs
        welcomeWindow = new WelcomeWindow(layout.center(600, 600), true, gameBackend, this);
        winWindow = new WinWindow(layout.center(580, 200), true, gameBackend, this);
        win2Window = new Win2Window(layout.center(580, 200), true, gameBackend, this);
        loseWindow = new LoseWindow(layout.center(480, 200), true, gameBackend, this);
        openGameWindow = new OpenGameWindow(layout.center(490, 330), true, gameBackend, this);
        // main game
        statusWindow = new StatusWindow(layout.getStatusPaneDim(), true, gameBackend, this);
        gameWindow = new GameWindow(layout.getCenterPaneDim(), true, gameBackend, this);
        mapWindow = new MapWindow(layout.getMapPaneDim(), true, gameBackend, this);
        logWindow = new LogWindow(layout.getLogPaneDim(), true, gameBackend, this);
        // popups in main game
        monsterInfoWindow = new MonsterInfoWindow(layout.getLogPaneDim(), false, gameBackend, this);
        worldMapWindow = new WorldMapWindow(layout.getCenterPaneDim(), true, gameBackend, this);
        helpWindow = new HelpWindow(layout.getCenterPaneDim(), true, gameBackend, this);

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

    // returns true if update needed.
    private boolean processEvent(GameEvent event) {
        switch (event) {
            case WON_GAME: open(this.winWindow); return true;
            case WON_GAME2: open(this.win2Window); return true;
            case LOST_GAME: open(this.loseWindow); return true;
            case EFFECT: return true;
            case UPDATE_NEED_REDRAW: return true;
            case IN_STORE: processShopEntry(); return true;
            case IN_PORTAL: choosePortal(); return true;
            case GOT_PET: choosePet(); return true;
            default: return false;
        }
    }

    public void update() {
        // Process any remaining events, if necessary.
        while (! events.isEmpty()) {
            boolean needsUpdate = processEvent(events.get(0));
            events.remove(0);
            if (needsUpdate) {
                dirty = true;
                return;
            }
        }

        // finished all visual effects; now process future actions
        KeyEvent keyEvent = keyEvents.poll();
        if (keyEvent != null) {
            processKey(keyEvent);
        }

        GameResult result = gameBackend.update();
        this.events.addAll(result.events);

        // process the events for the last result, so we can display
        // any changes immediately.
        while (! events.isEmpty()) {
            boolean needsUpdate = processEvent(events.get(0));
            events.remove(0);
            if (needsUpdate) {
                dirty = true;
                return;
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
            open(new ConfirmWindow(true, gameBackend, this,
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
                layout.center(490, 330),
                gameBackend, this,
                new Random(),
                Collections.singletonList("Select your character:"), false,
                this::tryToStartNewGame, () -> setState(Frontend.State.OPEN_GAME));
        open(createCharWindow);
    }

    private void choosePet() {
        SelectCharWindow createCharWindow = new SelectCharWindow(
                layout.center(490, 330),
                gameBackend, this,
                gameBackend.getGameState().rng,
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
        List<ShopData.ShopEntry> entries;
        switch (shopData.state) {
            case INN:
                int cost = shopData.innCost;
                open(new ConfirmWindow(true, gameBackend, this,
                        "Do you want to rest at the inn? It costs " + cost + " gold.",
                        "Rest", "Cancel",
                        () -> gameBackend.tellSelectedActor(new RestAtInn())));
                break;
            case WEAPON_SHOP:
                entries = shopData.weaponItems;
                open(new ShopWindow(true, gameBackend, this, entries));
                break;
            case MAGIC_SHOP:
                entries = shopData.magicItems;
                open(new ShopWindow(true, gameBackend, this, entries));
                break;
            case JEWELER_SHOP:
                open(new JewelerShopWindow(true, gameBackend, this,
                        (UpgradeItem.UpgradeInfo i) -> gameBackend.tellSelectedActor(new UpgradeItem(i))));
                break;
            default:
                System.out.println("entered a shop of type " + shopData.state);
        }
    }

    private void choosePortal() {
        Map<String, MapPoint.PortalStatus> portals = gameBackend.getGameState().world.topologySummary.getPortals();
        List<PortalChoiceWindow.AreaNameAndId> openPortals = new ArrayList<>();
        for (Map.Entry<String, MapPoint.PortalStatus> entry : portals.entrySet()) {
            if (entry.getValue() == MapPoint.PortalStatus.OPEN) {
                String id = entry.getKey();
                String name = gameBackend.getGameState().world.world.get(id).name;
                openPortals.add(new PortalChoiceWindow.AreaNameAndId(name, id));
            }
        }
        openPortals.sort(Comparator.comparing((PortalChoiceWindow.AreaNameAndId p) -> p.name));

        open(new PortalChoiceWindow(
                gameBackend, this,
                "Which area do you wish to go to?",
                openPortals,
                (String areaId) -> gameBackend.tellSelectedActor(new ExitPortal(gameBackend.getGameState().party.player, areaId))
        ));
    }

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

        graphics.setColor(Style.DESKTOP_COLOR);
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
        this.layout.setScreenSize(width, height);

        this.statusWindow.resize(layout.getStatusPaneDim());
        this.gameWindow.resize(layout.getCenterPaneDim());
        this.helpWindow.resize(layout.getCenterPaneDim());
        this.mapWindow.resize(layout.getMapPaneDim());
        this.logWindow.resize(layout.getLogPaneDim());
    }
}
