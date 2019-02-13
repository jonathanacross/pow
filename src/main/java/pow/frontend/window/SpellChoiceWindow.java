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

    public SpellChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                             String message,
                             List<SpellParams> spells,
                             Consumer<Integer> callback) {
        super( new WindowDim(x, y, 570, 105 + Style.getFontSize() * spells.size()),
                true, backend, frontend);
        this.message = message;
        this.spells = spells;
        this.spellTable = getSpellTable();
        this.callback = callback;
        this.resize(new WindowDim(x, y, spellTable.getWidth() + 2*Style.MARGIN, spellTable.getHeight() + 100));
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

        TableBuilder tableBuilder = new TableBuilder();

        List<Cell> header = new ArrayList<>();
        header.add(new EmptyCell());
        header.add(new TextCell(Arrays.asList("Spell"), TextCell.Style.NORMAL, font));
        header.add(new TextCell(Arrays.asList("Level"), TextCell.Style.NORMAL, font));
        header.add(new TextCell(Arrays.asList("Mana"), TextCell.Style.NORMAL, font));
        header.add(new TextCell(Arrays.asList("Info"), TextCell.Style.NORMAL, font));

        tableBuilder.addRow(header);
        int idx = 0;
        for (SpellParams spell : spells) {
            List<Cell> row = new ArrayList<>();
            TextCell.Style style = enabled(spell) ? TextCell.Style.NORMAL : TextCell.Style.DISABLED;

            String label = (char) ((int) 'a' + idx) + ")";
            idx++;
            String desc = spell.getDescription(backend.getGameState().party.selectedActor);

            row.add(new TextCell(Arrays.asList(label), style, font));
            row.add(new TextCell(Arrays.asList(spell.name), style, font));
            row.add(new TextCell(Arrays.asList(Integer.toString(spell.minLevel)), style, font));
            row.add(new TextCell(Arrays.asList(Integer.toString(spell.requiredMana)), style, font));
            row.add(new TextCell(Arrays.asList(desc), style, font));
            tableBuilder.addRow(row);
        }

        tableBuilder.setColWidths(Arrays.asList(20, 140, 40, 40, 300));
        tableBuilder.setDrawHeaderLine(true);
        Table spellTable = tableBuilder.build();

        return spellTable;
    }

    @Override
    public void drawContents(Graphics graphics) {

        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = Style.getDefaultFont();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(this.message, Style.MARGIN, Style.MARGIN + Style.getFontSize());

        spellTable.draw(graphics, Style.MARGIN, Style.MARGIN + 30);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select a spell, or press [esc] to cancel.", Style.MARGIN, dim.height - Style.MARGIN);
    }
}
