package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.table.*;

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
        TableBuilder spellListBuilder = new TableBuilder();
        spellListBuilder.addRow(Arrays.asList(
                new EmptyCell(),
                new TextCell(Arrays.asList("Spell"), State.NORMAL, font),
                new TextCell(Arrays.asList("Level"), State.NORMAL, font),
                new TextCell(Arrays.asList("Mana"), State.NORMAL, font),
                new TextCell(Arrays.asList("Info"), State.NORMAL, font)
        ));
        for (int i = 0; i < spells.size(); i++) {
            SpellParams spell = spells.get(i);
            State state = enabled(spell) ? State.NORMAL : State.DISABLED;

            String label = (char) ((int) 'a' + i) + ")";
            String desc = spell.getDescription(backend.getGameState().party.selectedActor);

            spellListBuilder.addRow(Arrays.asList(
                    new TextCell(Arrays.asList(label), state, font),
                    new TextCell(Arrays.asList(spell.name), state, font),
                    new TextCell(Arrays.asList(Integer.toString(spell.minLevel)), state, font),
                    new TextCell(Arrays.asList(Integer.toString(spell.requiredMana)), state, font),
                    new TextCell(Arrays.asList(desc), state, font)
            ));
        }
        spellListBuilder.setHSpacing(Style.MARGIN);
        spellListBuilder.setDrawHeaderLine(true);
        Table spellList = spellListBuilder.build();

        // build the full window
        TableBuilder tableBuilder = new TableBuilder();

        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList(this.message), State.NORMAL, font)
        ));
        tableBuilder.addRow(Arrays.asList(
                spellList
        ));
        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("Select a spell, or press [esc] to cancel."), State.NORMAL, font)
        ));

        tableBuilder.setVSpacing(Style.MARGIN);
        Table spellTable = tableBuilder.build();

        return spellTable;
    }

    @Override
    public void drawContents(Graphics graphics) {

        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        spellTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
