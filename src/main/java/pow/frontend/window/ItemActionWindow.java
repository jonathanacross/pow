package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.ItemActions;
import pow.util.TextUtils;

import java.awt.Color;
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
                        items, itemNumber, actions));
                // parent will close this window, too.
            }
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        graphics.drawString(message, Style.SMALL_MARGIN, Style.SMALL_MARGIN + Style.FONT_SIZE);

        int y = 30;
        int idx = 0;
        for (DungeonItem item : items.items) {
            String label = (char) ((int) 'a' + idx) + ")";
            graphics.drawString(label, Style.SMALL_MARGIN, y + 20);

            ImageController.drawTile(graphics, item.image, Style.SMALL_MARGIN + 20, y);
            graphics.drawString(TextUtils.format(item.name, item.count, false),  Style.SMALL_MARGIN + 60, y + Style.FONT_SIZE + 2);
            graphics.drawString(item.bonusString(), Style.SMALL_MARGIN + 60, y + 2*Style.FONT_SIZE + 2);

            idx++;
            y += Style.TILE_SIZE;
        }

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an item or press [esc] to cancel.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }
}
