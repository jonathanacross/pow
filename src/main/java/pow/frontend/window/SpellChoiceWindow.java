package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class SpellChoiceWindow extends AbstractWindow {

    private final String message;
    private final List<SpellParams> spells;
    private final Consumer<Integer> callback;

    public SpellChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                             String message,
                             List<SpellParams> spells,
                             Consumer<Integer> callback) {
        super( new WindowDim(x, y, 570, 105 + Style.FONT_SIZE * spells.size()),
                true, backend, frontend);
        this.message = message;
        this.spells = spells;
        this.callback = callback;
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

    @Override
    public void drawContents(Graphics graphics) {
        final int nameColumnX = Style.MARGIN + 20;
        final int levelColumnX = Style.MARGIN + 160;
        final int manaColumnX = Style.MARGIN + 200;
        final int infoColumnX = Style.MARGIN + 240;

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        graphics.drawString(this.message, Style.MARGIN, Style.MARGIN + Style.FONT_SIZE);

        int y = 55;
        graphics.drawString("Spell", nameColumnX,y);
        graphics.drawString("Level", levelColumnX,y);
        graphics.drawString("Mana", manaColumnX, y);
        graphics.drawString("Info", infoColumnX, y);

        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        graphics.drawLine(nameColumnX, y + 5, dim.width - Style.MARGIN, y + 5);

        graphics.setColor(Color.WHITE);

        y = 75;
        int idx = 0;
        for (SpellParams spell : spells) {
            boolean isEnabled = enabled(spell);

            String label = (char) ((int) 'a' + idx) + ")";
            graphics.setColor(isEnabled ? Color.WHITE : Color.GRAY);
            graphics.drawString(label, Style.MARGIN, y);
            graphics.drawString(spell.name, nameColumnX, y);
            graphics.drawString(Integer.toString(spell.minLevel), levelColumnX, y);
            graphics.drawString(Integer.toString(spell.requiredMana), manaColumnX, y);
            graphics.drawString(spell.getDescription(backend.getGameState().party.selectedActor), infoColumnX, y);

            idx++;
            y += Style.FONT_SIZE;
        }

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select a spell, or press [esc] to cancel.", Style.MARGIN, dim.height - Style.MARGIN);
    }
}
