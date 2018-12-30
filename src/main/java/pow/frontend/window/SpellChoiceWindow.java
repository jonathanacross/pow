package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.SpellParams;
import pow.frontend.Frontend;
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
        super( new WindowDim(x, y, 550, 60 + FONT_SIZE * spells.size()),
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

    private static final int MARGIN = 10;
    private static final int FONT_SIZE = 12;

    private boolean enabled(SpellParams params) {
        return params.minLevel <= backend.getGameState().party.selectedActor.level &&
                params.requiredMana <= backend.getGameState().party.selectedActor.getMana();
    }

    @Override
    public void drawContents(Graphics graphics) {
        final int nameColumnX = MARGIN + 20;
        final int levelColumnX = MARGIN + 160;
        final int manaColumnX = MARGIN + 200;
        final int infoColumnX = MARGIN + 240;

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(this.message, MARGIN, MARGIN + FONT_SIZE);

        int y = 45;
        graphics.drawString("Spell", nameColumnX,y);
        graphics.drawString("Level", levelColumnX,y);
        graphics.drawString("Mana", manaColumnX, y);
        graphics.drawString("Info", infoColumnX, y);

        y = 60;
        int idx = 0;
        for (SpellParams spell : spells) {
            boolean isEnabled = enabled(spell);

            String label = (char) ((int) 'a' + idx) + ")";
            graphics.setColor(isEnabled ? Color.WHITE : Color.GRAY);
            graphics.drawString(label, MARGIN, y);
            graphics.drawString(spell.name, nameColumnX, y);
            graphics.drawString(Integer.toString(spell.minLevel), levelColumnX, y);
            graphics.drawString(Integer.toString(spell.requiredMana), manaColumnX, y);
            graphics.drawString(spell.getDescription(backend.getGameState().party.selectedActor), infoColumnX, y);

            idx++;
            y += FONT_SIZE;
        }
    }
}
