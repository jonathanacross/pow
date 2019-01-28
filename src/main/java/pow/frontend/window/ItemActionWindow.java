package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.ItemActions;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;

public class ItemActionWindow extends AbstractWindow {

    private final String message;
    private final ItemList items;
    private final ItemActions.ItemLocation location;

    public ItemActionWindow(int x, int y, GameBackend backend, Frontend frontend,
                            String message,
                            ItemList items,
                            ItemActions.ItemLocation location) {
        super(new WindowDim(x, y, 400,
                55 + 32 * items.size()), true, backend, frontend);
        this.message = message;
        this.items = items;
        this.location = location;
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
            if (itemNumber >= 0 && itemNumber < items.size()) {
                GameState gs = backend.getGameState();
                DungeonItem item = items.get(itemNumber);
                List<ItemActions.Action> actions = ItemActions.getActions(item, gs, location);
                // try to place the window near the item
                int x = this.dim.x + 20;
                int y = Math.min(this.dim.y + 32 * itemNumber, this.frontend.height - 140);
                frontend.open(new ActionChoiceWindow(x, y, this.backend, this.frontend,
                        "What do you want to do with ",
                        items, itemNumber, location, actions));
                // parent will close this window, too.
            }
        }
    }

    final private int TILE_SIZE = 32;
    final private int MARGIN = 10;
    final private int FONT_SIZE = 12;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(message, MARGIN, MARGIN + FONT_SIZE);

        int y = 30;
        int idx = 0;
        for (DungeonItem item : items.items) {
            String label = (char) ((int) 'a' + idx) + ")";
            graphics.drawString(label, MARGIN, y + 20);

            ImageController.drawTile(graphics, item.image, MARGIN + 20, y);
            graphics.drawString(TextUtils.format(item.name, item.count, false),  MARGIN + 60, y + FONT_SIZE + 2);
            graphics.drawString(item.bonusString(), MARGIN + 60, y + 2*FONT_SIZE + 2);

            idx++;
            y += TILE_SIZE;
        }

        Font helpFont = new Font("Courier", Font.PLAIN, 12);
        graphics.setFont(helpFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an item or press [esc] to cancel.", MARGIN, dim.height - MARGIN);
    }
}
