package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class ItemChoiceWindow extends AbstractWindow {

    private String message;
    private List<DungeonItem> items;
    private Function<DungeonItem, Boolean> enabled;
    private IntConsumer callback;

    public ItemChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                            String message,
                            List<DungeonItem> items,
                            Function<DungeonItem, Boolean> enabled,
                            IntConsumer callback) {
        super(x, y, 250, 35 + 32 * items.size(), true, backend, frontend);
        this.message = message;
        this.items = items;
        this.enabled = enabled;
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
            int itemNumber = keyCode - KeyEvent.VK_A;
            this.callback.accept(itemNumber);
            frontend.close();
        }
    }

    final private int TILE_SIZE = 32;
    final private int MARGIN = 10;
    final private int FONT_SIZE = 12;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString(this.message, MARGIN, MARGIN + FONT_SIZE);

        int y = 30;

        int idx = 0;
        for (DungeonItem item : this.items) {
            boolean isEnabled = enabled.apply(item);
            ImageController.drawTile(graphics, item.image, 25, y, !isEnabled);

            String label = (char) ((int) 'a' + idx) + ")";
            int textY = y + 20;
            graphics.setColor(isEnabled ? Color.WHITE : Color.GRAY);
            graphics.drawString(label, MARGIN, textY);
            graphics.drawString(TextUtils.format(item.name, item.count, false), 60, textY);

            idx++;
            y += TILE_SIZE;
        }
    }
}
