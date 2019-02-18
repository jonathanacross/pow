package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.ShopData;
import pow.backend.action.BuyItem;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.widget.*;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopWindow extends AbstractWindow {
    private final List<ShopData.ShopEntry> entries;
    private final Table layoutTable;

    public ShopWindow(boolean visible, GameBackend backend, Frontend frontend,
                      List<ShopData.ShopEntry> entries) {
        super(new WindowDim(0,0,0,0), visible, backend, frontend);
        this.entries = entries;
        this.layoutTable = getLayoutTable();
        int width = layoutTable.getWidth() + 2*Style.MARGIN;
        int height = layoutTable.getHeight() + 2*Style.MARGIN;
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
            if (itemNumber < entries.size()) {
                ShopData.ShopEntry entry = entries.get(itemNumber);
                int maxNum = maxNumBuyable(entry);
                if (maxNum > 0) {
                    WindowDim cDim = new WindowDim(dim.x, dim.y, dim.width, 120);
                    List<String> messages = new ArrayList<>();
                    messages.add("How many " + TextUtils.plural(entry.item.name));
                    String bonus = entry.item.bonusString();
                    if (!bonus.isEmpty()) {
                        messages.add(entry.item.bonusString());
                    }
                    messages.add("do you want (max " + maxNum + ")?");
                    frontend.open(new GetCountWindow(cDim, this.backend, this.frontend,
                            entry.item.image, messages, maxNum,
                            (int count) -> backend.tellSelectedActor(new BuyItem(entries, itemNumber, count))));
                }
            }
        }
    }

    private int maxNumBuyable(ShopData.ShopEntry entry) {
        GameState gs = backend.getGameState();
        DungeonItem item = entry.item;

        // Number you can buy is limited by gold, by the count in
        // the shop, and the number the player can carry.
        int maxNum = gs.party.player.gold / entry.price;
        maxNum = Math.min(maxNum, item.count);
        maxNum = Math.min(maxNum, gs.party.player.inventory.numCanAdd(item));

        return maxNum;
    }

    private Table getLayoutTable() {
        Font font = Style.getDefaultFont();

        // build the inner list
        Table list = new Table();
        list.addRow(Arrays.asList(
                new TableCell(new Space()),
                new TableCell(new TextBox(Arrays.asList("Item"), State.NORMAL, font)),
                new TableCell(new Space()),
                new TableCell(new TextBox(Arrays.asList("Price"), State.NORMAL, font))
        ));
        for (int i = 0; i < this.entries.size(); i++) {
            ShopData.ShopEntry entry = entries.get(i);
            boolean isGrayed = maxNumBuyable(entry) == 0;
            State state = isGrayed ? State.DISABLED : State.NORMAL;

            String label = (char) ((int) 'a' + i) + ")";
            List<String> itemInfo = Arrays.asList(TextUtils.format(entry.item.name, entry.item.count, false), entry.item.bonusString());
            String price = String.valueOf(entry.price);

            list.addRow(Arrays.asList(
                    new TableCell(new TextBox(Arrays.asList(label), state, font)),
                    new TableCell(new Tile(entry.item.image, state)),
                    new TableCell(new TextBox(itemInfo, state, font)),
                    new TableCell(new TextBox(Arrays.asList(price), state, font))
            ));
        }
        list.setDrawHeaderLine(true);
        list.setHSpacing(Style.MARGIN);
        list.autosize();

        // build the overall layout
        Table layout = new Table();
        String greeting = "Hi " + backend.getGameState().party.player.name + ", what would you like to buy?";
        String help = "Select an item to buy or press [esc] to cancel.";
        layout.addColumn(Arrays.asList(
                new TableCell(new TextBox(Arrays.asList(greeting), State.NORMAL, font)),
                new TableCell(list),
                new TableCell(new TextBox(Arrays.asList(help), State.NORMAL, font))
        ));
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();

        return layout;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        layoutTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
