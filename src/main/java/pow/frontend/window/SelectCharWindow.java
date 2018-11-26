package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.backend.dungeon.gen.CharacterGenerator;
import pow.backend.dungeon.gen.NameGenerator;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.*;
import pow.util.MathUtils;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectCharWindow extends AbstractWindow {

    public static class NamedCharData {
        public String name;
        public CharacterGenerator.CharacterData characterData;

        public NamedCharData(String name, CharacterGenerator.CharacterData characterData) {
            this.name = name;
            this.characterData = characterData;
        }
    }

    private String name;
    private boolean onName;
    private int charSelectId;
    private final List<CharacterGenerator.CharacterData> characterData;
    private Consumer<NamedCharData> successCallback;
    private Runnable cancelCallback;
    private List<String> messages;
    private boolean showSpeed;

    public SelectCharWindow(WindowDim dim, GameBackend backend, Frontend frontend,
                            List<String> messages, boolean showPets,
                            Consumer<NamedCharData> successCallback, Runnable cancelCallback) {
        super(dim, true, backend, frontend);
        this.name = "";
        this.onName = false;
        this.charSelectId = 0;
        this.characterData = showPets
                ? CharacterGenerator.getPetCharacterData()
                : CharacterGenerator.getPlayerCharacterData();
        this.messages = messages;
        this.successCallback = successCallback;
        this.cancelCallback = cancelCallback;
        this.showSpeed = showPets;  // only show speed stat for pets
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
                NamedCharData data = new NamedCharData(name, characterData.get(charSelectId));
                successCallback.accept(data);
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
                cancelCallback.run();
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

    // TODO: same function as in MonsterDisplay.java
    private static List<String> getSpellNames(List<SpellParams> spells) {
        List<String> strings = new ArrayList<>(spells.size());
        for (SpellParams spell : spells) {
            strings.add(spell.name);
        }
        return strings;
    }

    private void drawBar(Graphics graphics, int x, int y, int width, int height, double gain) {
        Color saveColor = graphics.getColor();

        // min/max expected values for gains.
        double min = 0.5;
        double max = 1.5;

        // Convert the gain to red-yellow-green color scale.
        double z = MathUtils.clamp((gain - min) / (max - min), 0, 1);

        int red   = (int) (255.0 * MathUtils.clamp(2.0 * (1 - z), 0, 1));
        int green = (int) (255.0 * MathUtils.clamp(2.0 * z, 0, 1));
        int blue  = 0;

        Color fillColor = new Color(red, green, blue);

        graphics.setColor(fillColor);
        graphics.fillRect(x, y+3, width, height-1);

        graphics.setColor(saveColor);
    }

    private void drawCharData(Graphics graphics, CharacterGenerator.CharacterData characterData, int x, int y, boolean showSpeed) {
        int barLeft = x + 60;
        String[] labels = {"Str   ", "Dex   ", "Int   ", "Con   ", "Speed "};
        double[] gains = {characterData.strGain, characterData.dexGain, characterData.intGain, characterData.conGain, characterData.speedGain};
        int maxGains = showSpeed ? gains.length : gains.length - 1;
        for (int i = 0; i < maxGains; i++) {
            int width = (int) Math.round(gains[i] * 100);
            drawBar(graphics, barLeft, y + i * FONT_SIZE, width, FONT_SIZE, gains[i]);
            graphics.drawString(labels[i], x, y + (i+1) * FONT_SIZE);
        }
        y += (maxGains + 1) * FONT_SIZE;

        int textWidth = dim.width - 2*MARGIN;
        Font font = graphics.getFont();
        FontMetrics textMetrics = graphics.getFontMetrics(font);
        List<String> spellLines =
        ImageUtils.wrapText(
                "Spells: " + TextUtils.formatList(getSpellNames(characterData.spells)), textMetrics, textWidth);
        for (String line : spellLines) {
            y += FONT_SIZE;
            graphics.drawString(line, x, y);
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        int y = MARGIN + FONT_SIZE;
        for (String message : messages) {
            graphics.drawString(message, MARGIN, y);
            y += 2 * FONT_SIZE;
        }
        y -= FONT_SIZE;
        for (int i = 0; i < characterData.size(); i++) {
            ImageController.drawTile(graphics, characterData.get(i).image, MARGIN + i*(ImageController.TILE_SIZE + 5), y);
        }
        graphics.setColor(Color.YELLOW);
        graphics.drawRect(MARGIN + charSelectId*(ImageController.TILE_SIZE + 5), y, ImageController.TILE_SIZE, ImageController.TILE_SIZE);
        y += ImageController.TILE_SIZE + FONT_SIZE;

        if (onName) {
            graphics.setColor(Color.WHITE);
            y += FONT_SIZE;
            graphics.drawString("Enter the name of your character,", MARGIN, y);
            y += FONT_SIZE;
            graphics.drawString("or press * for a random name:", MARGIN, y);
            y += 2*FONT_SIZE;
            graphics.drawString(name, MARGIN, y);
        } else {
            graphics.setColor(Color.WHITE);
            drawCharData(graphics, characterData.get(charSelectId), MARGIN, y, showSpeed);
        }
    }
}
