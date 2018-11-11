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
import pow.util.MathUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class SetPetWindow extends AbstractWindow {

    private String name;
    private boolean onName;
    private int charSelectId;
    private final List<CharacterGenerator.CharacterData> characterData;

    public SetPetWindow(WindowDim dim, GameBackend backend, Frontend frontend) {
        super(dim, true, backend, frontend);
        resetName();
        onName = false;
        charSelectId = 0;
        characterData = CharacterGenerator.getPetCharacterData();
    }

    private void resetName() {
        name = "";
    }

    private void addPetToGame() {
        Player pet = CharacterGenerator.getPlayer(name, characterData.get(charSelectId).id);
        backend.setPet(pet);
        frontend.close();
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
                addPetToGame();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
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

    private static final int FONT_SIZE = 14;
    private static final int MARGIN = 20;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        int y = MARGIN + FONT_SIZE;
        graphics.drawString("Congratulations, you got a pet!", MARGIN, y);
        y += FONT_SIZE * 2;
        graphics.drawString("Select your pet:", MARGIN, y);
        y += FONT_SIZE * 2;

        for (int i = 0; i < characterData.size(); i++) {
            ImageController.drawTile(graphics, characterData.get(i).image, MARGIN + i*(ImageController.TILE_SIZE + 5), y);
        }
        graphics.setColor(Color.YELLOW);
        graphics.drawRect(MARGIN + charSelectId*(ImageController.TILE_SIZE + 5), y, ImageController.TILE_SIZE, ImageController.TILE_SIZE);
        y += ImageController.TILE_SIZE + 2*FONT_SIZE;

        if (onName) {
            graphics.setColor(Color.WHITE);
            graphics.drawString("Enter the name of your character,", MARGIN, y);
            y += FONT_SIZE;
            graphics.drawString("or press * for a random name:", MARGIN, y);
            y += 2*FONT_SIZE;
            graphics.drawString(name, MARGIN, y);
        }
    }
}
