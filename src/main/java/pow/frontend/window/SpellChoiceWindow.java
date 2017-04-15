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
        super(new WindowDim(x, y, 350,
                50 + FONT_SIZE * spells.size()),
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
            if (spellNumber >= 0 && spellNumber < spells.size() &&
                    enabled(spells.get(spellNumber))) {
                this.callback.accept(spellNumber);
                frontend.close();
            }
        }
    }

    private static final int MARGIN = 10;
    private static final int FONT_SIZE = 12;

    private boolean enabled(SpellParams params) {
        return params.minLevel <= backend.getGameState().player.level;
    }

    @Override
    public void drawContents(Graphics graphics) {
        String currMessage = this.message;

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(currMessage, MARGIN, MARGIN + FONT_SIZE);

        int y = 30;
        int idx = 0;
        for (SpellParams spell : spells) {
            boolean isEnabled = enabled(spell);

            String label = (char) ((int) 'a' + idx) + ")";
            int textY = y + 20;
            graphics.setColor(isEnabled ? Color.WHITE : Color.GRAY);
            graphics.drawString(label, MARGIN, textY);
            graphics.drawString(spell.name, 40, textY);
            graphics.drawString(spell.getDescription(backend.getGameState().player), 150, textY);

            idx++;
            y += FONT_SIZE;
        }
    }
}
