package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.table.Cell;
import pow.frontend.utils.table.EmptyCell;
import pow.frontend.utils.table.TableBuilder;
import pow.frontend.utils.table.Table;
import pow.frontend.utils.table.TextCell;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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
                new TextCell(Arrays.asList("Spell"), TextCell.Style.NORMAL, font),
                new TextCell(Arrays.asList("Level"), TextCell.Style.NORMAL, font),
                new TextCell(Arrays.asList("Mana"), TextCell.Style.NORMAL, font),
                new TextCell(Arrays.asList("Info"), TextCell.Style.NORMAL, font)
        ));
        for (int i = 0; i < spells.size(); i++) {
            SpellParams spell = spells.get(i);
            TextCell.Style style = enabled(spell) ? TextCell.Style.NORMAL : TextCell.Style.DISABLED;

            String label = (char) ((int) 'a' + i) + ")";
            String desc = spell.getDescription(backend.getGameState().party.selectedActor);

            spellListBuilder.addRow(Arrays.asList(
                    new TextCell(Arrays.asList(label), style, font),
                    new TextCell(Arrays.asList(spell.name), style, font),
                    new TextCell(Arrays.asList(Integer.toString(spell.minLevel)), style, font),
                    new TextCell(Arrays.asList(Integer.toString(spell.requiredMana)), style, font),
                    new TextCell(Arrays.asList(desc), style, font)
            ));
        }
        spellListBuilder.setHSpacing(Style.MARGIN);
        spellListBuilder.setDrawHeaderLine(true);
        Table spellList = spellListBuilder.build();

        // build the full window
        TableBuilder tableBuilder = new TableBuilder();

        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList(this.message), TextCell.Style.NORMAL, font)
        ));
        tableBuilder.addRow(Arrays.asList(
                spellList
        ));
        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("Select a spell, or press [esc] to cancel."), TextCell.Style.NORMAL, font)
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
