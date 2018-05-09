package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.actors.Player;
import pow.backend.dungeon.gen.CharacterGenerator;
import pow.backend.dungeon.gen.NameGenerator;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.utils.SaveUtils;
import pow.util.MathUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

public class CreateCharWindow extends AbstractWindow {

    private String name;
    private boolean onName;
    private int charSelectId;
    private final List<CharacterGenerator.CharacterData> characterData;

    public CreateCharWindow(WindowDim dim, GameBackend backend, Frontend frontend) {
        super(dim, true, backend, frontend);
        resetName();
        onName = false;
        charSelectId = 0;
        characterData = CharacterGenerator.getCharacterData();
    }

    public void resetName() {
        name = "";
    }

    private void startNewGame() {
        Player player = CharacterGenerator.getPlayer(name, characterData.get(charSelectId).id);
        onName = false;
        backend.newGame(player);
        frontend.setState(Frontend.State.GAME);
    }

    // Start the game if
    // (1) it's a new character name, or
    // (2) an existing character name and user has confirmed they want to overwrite.
    private void tryToStartNewGame() {
        // see if there's already a character with this name
        List<File> existingFiles = SaveUtils.findSaveFiles();
        boolean alreadyExists = false;
        for (File f : existingFiles) {
            if (f.getName().equals(name)) {
                alreadyExists = true;
            }
        }

        if (alreadyExists) {
            WindowDim dim = WindowDim.center(600, 120, frontend.width, frontend.height);
            frontend.open(new ConfirmWindow(dim, true, this.backend, this.frontend,
                    "The character '" + name + "' already exists.  Do you want to overwrite it?",
                    "Overwrite", "Cancel",
                    this::startNewGame));
        } else {
            startNewGame();
        }
    }

    private void processNameKey(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == '*') {
            name = NameGenerator.getRandomName(backend.getGameState().rng);
            frontend.setDirty(true);
        } else if (Character.isLetterOrDigit(c) || c == ' ') {
            name = name + c;
            frontend.setDirty(true);
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (name.length() > 0) {
                name = name.substring(0, name.length() - 1);
                frontend.setDirty(true);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            name = name.trim();
            if (!name.isEmpty()) {
                tryToStartNewGame();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_UP) {
            onName = false;
            frontend.setDirty(true);
        }
    }

    private void processSelectKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case EAST:
                charSelectId++;
                break;
            case WEST:
                charSelectId--;
                break;
            case SOUTH:
            case OKAY:
                onName = true;
                break;
            case CANCEL:
                frontend.setState(Frontend.State.OPEN_GAME);
                break;
            default: break;
        }
        charSelectId = MathUtils.clamp(charSelectId, 0, characterData.size() - 1);
        frontend.setDirty(true);
    }

    @Override
    public void processKey(KeyEvent e) {
        if (onName) {
            processNameKey(e);
        } else {
            processSelectKey(e);
        }
    }

    private static final int FONT_SIZE = 18;
    private static final int MARGIN = 20;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString("Select your character:", MARGIN, MARGIN + FONT_SIZE);
        int y = MARGIN + FONT_SIZE + 10;
        for (int i = 0; i < characterData.size(); i++) {
            ImageController.drawTile(graphics, characterData.get(i).image, MARGIN + i*(ImageController.TILE_SIZE + 5), y);
        }
        graphics.setColor(Color.YELLOW);
        graphics.drawRect(MARGIN + charSelectId*(ImageController.TILE_SIZE + 5), y, ImageController.TILE_SIZE, ImageController.TILE_SIZE);

        if (onName) {
            graphics.setColor(Color.WHITE);
            graphics.drawString("Enter the name of your character,", MARGIN, MARGIN + 6*FONT_SIZE);
            graphics.drawString("or press * for a random name:", MARGIN, MARGIN + 7 * FONT_SIZE);
            graphics.drawString(name, MARGIN, MARGIN + 8 * FONT_SIZE + 10);
        }
    }
}
