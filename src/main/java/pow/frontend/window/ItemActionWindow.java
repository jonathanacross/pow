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
import pow.frontend.utils.table.Cell;
import pow.frontend.utils.table.EmptyCell;
import pow.frontend.utils.table.ImageCell;
import pow.frontend.utils.table.Table;
import pow.frontend.utils.table.TableBuilder;
import pow.frontend.utils.table.TextCell;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemActionWindow extends AbstractWindow {

    private final String message;
    private final ItemList items;
    private final ItemActions.ItemLocation location;
    private final Table itemTable;

    public ItemActionWindow(int x, int y, GameBackend backend, Frontend frontend,
                            String message,
                            ItemList items,
                            ItemActions.ItemLocation location) {
        super(new WindowDim(x, y, 400,
                55 + 34 * items.size()), true, backend, frontend);
        this.message = message;
        this.items = items;
        this.location = location;
        this.itemTable = getItemTable(items);
        int height = 3*Style.SMALL_MARGIN + Style.FONT_SIZE + itemTable.getHeight();
        this.resize(frontend.layout.center(400, height));
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

        TableBuilder tableBuilder = new TableBuilder();
        List<Cell> header = new ArrayList<>();
        header.add(new TextCell(Arrays.asList(message), TextCell.Style.NORMAL, font));
        header.add(new EmptyCell());
        header.add(new EmptyCell());
        tableBuilder.addRow(header);

        int idx = 0;
        for (DungeonItem item : items.items) {
            List<Cell> row = new ArrayList<>();
            String label = (char) ((int) 'a' + idx) + ")";
            List<String> itemInfo = Arrays.asList(TextUtils.format(item.name, item.count, false),  item.bonusString());

            row.add(new TextCell(Arrays.asList(label), TextCell.Style.NORMAL, font));
            row.add(new ImageCell(item.image, false));
            row.add(new TextCell(itemInfo, TextCell.Style.NORMAL, font));
            tableBuilder.addRow(row);

            idx++;
        }

        tableBuilder.setDrawHeaderLine(true);
        tableBuilder.setSpacing(2);
        tableBuilder.setColWidths(Arrays.asList(20, Style.TILE_SIZE + Style.SMALL_MARGIN, 300));
        Table itemTable = tableBuilder.build();

        return itemTable;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = Style.getDefaultFont();
        graphics.setFont(font);

        itemTable.draw(graphics, Style.SMALL_MARGIN, Style.SMALL_MARGIN);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an item or press [esc] to cancel.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }
}
