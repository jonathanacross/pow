package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.backend.actors.Abilities;
import pow.backend.dungeon.gen.CharacterGenerator;
import pow.backend.dungeon.gen.NameGenerator;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.*;
import pow.frontend.widget.*;
import pow.util.MathUtils;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SelectCharWindow extends AbstractWindow {

    public static class NamedCharData {
        public final String name;
        public final CharacterGenerator.CharacterData characterData;

        public NamedCharData(String name, CharacterGenerator.CharacterData characterData) {
            this.name = name;
            this.characterData = characterData;
        }
    }

    private String name;
    private boolean onName;
    private int charSelectId;
    private final List<CharacterGenerator.CharacterData> characterData;
    private final Consumer<NamedCharData> successCallback;
    private final Runnable cancelCallback;
    private final List<String> messages;
    private final boolean showSpeed;
    private final boolean allowCancel;

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
        this.allowCancel = !showPets;
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

    // TODO: same function as in MonsterDisplay.java
    private static List<String> getSpellNames(List<SpellParams> spells) {
        List<String> strings = new ArrayList<>(spells.size());
        for (SpellParams spell : spells) {
            strings.add(spell.name);
        }
        return strings;
    }

    private String getAbilityString(Abilities abilities) {
        List<String> abilityStrings = new ArrayList<>();
        if (abilities.archeryBonus) {
            abilityStrings.add("especially good with bows");
        }
        if (abilities.poisonDamage) {
            abilityStrings.add("may do extra poison damage");
        }
        if (abilities.stunDamage) {
            abilityStrings.add("may do extra stun damage");
        }
        return TextUtils.formatList(abilityStrings);
    }

    // Convert the gain to red-yellow-green color scale.
    private Color getBarFillColor(double gain) {
        // min/max expected values for gains.
        double min = 0.5;
        double max = 1.5;

        double z = MathUtils.clamp((gain - min) / (max - min), 0, 1);

        int red   = (int) (255.0 * MathUtils.clamp(2.0 * (1 - z), 0, 1));
        int green = (int) (255.0 * MathUtils.clamp(2.0 * z, 0, 1));
        int blue  = 0;

        Color fillColor = new Color(red, green, blue);
        return fillColor;
    }

    private Table getCharInfoTable(CharacterGenerator.CharacterData characterData, Font font, int textWidth) {
        String[] labels = {"Str", "Dex", "Int", "Con", "Speed"};
        double[] gains = {characterData.strGain, characterData.dexGain, characterData.intGain, characterData.conGain, characterData.speedGain};
        int maxGains = showSpeed ? gains.length : gains.length - 1;

        Table statsTable = new Table();
        for (int i = 0; i < maxGains; i++) {
            int barWidth = (int) Math.round(gains[i] * 100);
            statsTable.addRow(Arrays.asList(
                    new TextBox(Arrays.asList(labels[i]), State.NORMAL, font),
                    new Bar(barWidth, barWidth, getBarFillColor(gains[i]), "", font)
            ));
        }
        statsTable.setHSpacing(Style.MARGIN);
        statsTable.autosize();

        TextBox abilities = new TextBox(
                Arrays.asList(
                        "Abilities: " + getAbilityString(characterData.abilities),
                        "Spells: " + TextUtils.formatList(getSpellNames(characterData.spells))),
                State.NORMAL, font, textWidth);

        Table layout = new Table();
        layout.addColumn(Arrays.asList(
                statsTable,
                abilities
        ));
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();

        return layout;
    }

    private Table getLayout(int width) {
        Font font = Style.getDefaultFont();

        // messages at top
        TextBox messagesBox = new TextBox(messages, State.NORMAL, font);

        // build character list
        Table charList = new Table();
        for (int i = 0; i < characterData.size(); i++) {
            State state = i == charSelectId ? State.SELECTED : State.NORMAL;
            charList.addColumn(Arrays.asList(
                    new Tile(characterData.get(i).image, state)
            ));
        }
        charList.setHSpacing(Style.SMALL_MARGIN);
        charList.autosize();

        // place to enter name
        TextBox nameBox = new TextBox(
                Arrays.asList(
                        "Enter the name of your character:",
                        "",
                        "> " + name),
                State.NORMAL, font);

        // Help message
        String helpChooseString = allowCancel
                ? "Press left/right to choose, [enter] to select, [esc] to cancel."
                : "Press left/right to choose, [enter] to select.";
        String helpString = onName
                ? "Press * for a random name, [enter] to select, [esc] to cancel."
                : helpChooseString;
        TextBox helpBox = new TextBox(Arrays.asList(helpString), State.NORMAL, font);

        // overall layout
        Table layout = new Table();
        layout.addRow(Arrays.asList(messagesBox));
        layout.addRow(Arrays.asList(charList));
        if (onName) {
            layout.addRow(Arrays.asList(nameBox));
        } else {
            layout.addRow(Arrays.asList(getCharInfoTable(characterData.get(charSelectId), font, width)));
        }
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();

        return layout;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Table layout = getLayout(dim.width - 2*Style.MARGIN);
        layout.draw(graphics, Style.MARGIN, Style.MARGIN);

        String helpChooseString = allowCancel
                ? "Press left/right to choose, [enter] to select, [esc] to cancel."
                : "Press left/right to choose, [enter] to select.";
        String helpString = onName
                ? "Press * for a random name, [enter] to select, [esc] to cancel."
                : helpChooseString;
        graphics.setColor(Color.WHITE);
        graphics.drawString(helpString, Style.MARGIN, dim.height - Style.MARGIN);
    }
}
