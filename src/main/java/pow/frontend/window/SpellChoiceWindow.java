package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.widget.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SpellChoiceWindow extends AbstractWindow {

    private final String message;
    private final List<SpellParams> spells;
    private final Consumer<Integer> callback;
    private final Table spellTable;

    public SpellChoiceWindow(GameBackend backend, Frontend frontend,
                             String message,
                             List<SpellParams> spells,
                             Consumer<Integer> callback) {
        super(new WindowDim(0, 0, 0, 0), true, backend, frontend);
        this.message = message;
        this.spells = spells;
        this.spellTable = getSpellTable();
        this.callback = callback;
        this.resize(frontend.layout.center(spellTable.getWidth() + 2*Style.MARGIN,
                spellTable.getHeight() + 2*Style.MARGIN));
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            frontend.close();
            return;
        }

        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            int spellNumber = keyCode - KeyEvent.VK_A;
            if (spellNumber < spells.size() &&
                    enabled(spells.get(spellNumber))) {
                this.callback.accept(spellNumber);
                frontend.close();
            }
        }
    }

    private boolean enabled(SpellParams params) {
        return params.minLevel <= backend.getGameState().party.selectedActor.level &&
                params.requiredMana <= backend.getGameState().party.selectedActor.getMana();
    }

    private Table getSpellTable() {
        Font font = Style.getDefaultFont();

        // Build the inner spell list
        Table spellList = new Table();
        spellList.addRow(Arrays.asList(
                new Space(),
                new TextBox(Arrays.asList("Spell"), State.NORMAL, font),
                new TextBox(Arrays.asList("Level"), State.NORMAL, font),
                new TextBox(Arrays.asList("Mana"), State.NORMAL, font),
                new TextBox(Arrays.asList("Info"), State.NORMAL, font)
        ));
        for (int i = 0; i < spells.size(); i++) {
            SpellParams spell = spells.get(i);
            State state = enabled(spell) ? State.NORMAL : State.DISABLED;

            String label = (char) ((int) 'a' + i) + ")";
            String desc = spell.getDescription(backend.getGameState().party.selectedActor);

            spellList.addRow(Arrays.asList(
                    new TextBox(Arrays.asList(label), state, font),
                    new TextBox(Arrays.asList(spell.name), state, font),
                    new TextBox(Arrays.asList(Integer.toString(spell.minLevel)), state, font),
                    new TextBox(Arrays.asList(Integer.toString(spell.requiredMana)), state, font),
                    new TextBox(Arrays.asList(desc), state, font)
            ));
        }
        spellList.setHSpacing(Style.MARGIN);
        spellList.setDrawHeaderLine(true);
        spellList.autosize();

        // build the full window
        Table layout = new Table();
        layout.addColumn(Arrays.asList(
                new TextBox(Arrays.asList(this.message), State.NORMAL, font),
                spellList,
                new TextBox(Arrays.asList("Select a spell, or press [esc] to cancel."), State.NORMAL, font)
        ));
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();

        return layout;
    }

    @Override
    public void drawContents(Graphics graphics) {

        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        spellTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
