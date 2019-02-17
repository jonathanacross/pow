package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ItemActions;
import pow.frontend.widget.*;
import pow.util.TextUtils;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

public class ItemActionWindow extends AbstractWindow {

    private final String message;
    private final ItemList items;
    private final ItemActions.ItemLocation location;
    private final Table itemTable;

    public ItemActionWindow(GameBackend backend, Frontend frontend,
                            String message,
                            ItemList items,
                            ItemActions.ItemLocation location) {
        super(new WindowDim(0, 0, 0, 0), true, backend, frontend);
        this.message = message;
        this.items = items;
        this.location = location;
        this.itemTable = getItemTable(items);
        int height = 2*Style.MARGIN + itemTable.getHeight();
        int width = 2*Style.MARGIN + itemTable.getWidth();
        this.resize(frontend.layout.center(width, height));
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

    private Table getItemTable(ItemList items) {
        Font font = Style.getDefaultFont();

        // build the list in the middle
        Table itemList = new Table();
        for (int i = 0; i < items.items.size(); i++) {
            DungeonItem item = items.items.get(i);
            String label = (char) ((int) 'a' + i) + ")";
            List<String> itemInfo = Arrays.asList(TextUtils.format(item.name, item.count, false),  item.bonusString());
            itemList.addRow(Arrays.asList(
                    new TextBox(Arrays.asList(label), State.NORMAL, font),
                    new Tile(item.image, State.NORMAL),
                    new TextBox(itemInfo, State.NORMAL, font)
            ));
        }
        itemList.setHSpacing(Style.MARGIN);
        itemList.autosize();

        // make the outer widget with header and footer
        Table table = new Table();
        table.addColumn(Arrays.asList(
                new TextBox(Arrays.asList(message), State.NORMAL, font),
                itemList,
                new TextBox(Arrays.asList("Select an item or press [esc] to cancel."), State.NORMAL, font)
        ));
        table.setVSpacing(Style.MARGIN);
        table.autosize();

        return table;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        itemTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
