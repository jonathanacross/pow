package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.ShopData;
import pow.backend.action.BuyItem;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.table.EmptyCell;
import pow.frontend.utils.table.ImageCell;
import pow.frontend.utils.table.Table;
import pow.frontend.utils.table.TableBuilder;
import pow.frontend.utils.table.TextCell;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopWindow extends AbstractWindow {
    private final List<ShopData.ShopEntry> entries;
    private final Table shopTable;

    public ShopWindow(boolean visible, GameBackend backend, Frontend frontend,
                      List<ShopData.ShopEntry> entries) {
        super(new WindowDim(0,0,0,0), visible, backend, frontend);
        this.entries = entries;
        this.shopTable = getShopTable();
        int width = shopTable.getWidth() + 2*Style.MARGIN;
        int height = shopTable.getHeight() + 2*Style.MARGIN + 5*Style.FONT_SIZE;
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
                    messages.add("do you want? (max " + maxNum + ")?");
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

    private Table getShopTable() {
        Font font = Style.getDefaultFont();

        TableBuilder builder = new TableBuilder();

        builder.addRow(Arrays.asList(
                new EmptyCell(),
                new TextCell(Arrays.asList("Item"), TextCell.Style.NORMAL, font),
                new EmptyCell(),
                new TextCell(Arrays.asList("Price"), TextCell.Style.NORMAL, font)
        ));

        int idx = 0;
        for (ShopData.ShopEntry entry : this.entries) {
            boolean isGrayed = maxNumBuyable(entry) == 0;
            TextCell.Style textStyle = isGrayed ? TextCell.Style.DISABLED : TextCell.Style.NORMAL;

            String label = (char) ((int) 'a' + idx) + ")";
            List<String> itemInfo = Arrays.asList(TextUtils.format(entry.item.name, entry.item.count, false), entry.item.bonusString());
            String price = String.valueOf(entry.price);

            builder.addRow(Arrays.asList(
                    new TextCell(Arrays.asList(label), textStyle, font),
                    new ImageCell(entry.item.image, isGrayed),
                    new TextCell(itemInfo, textStyle, font),
                    new TextCell(Arrays.asList(price), textStyle, font)
            ));

            idx++;
        }

        builder.setDrawHeaderLine(true);
        builder.setSpacing(2);
        builder.setColWidths(Arrays.asList(20, Style.TILE_SIZE + Style.SMALL_MARGIN, 250, 50));
        Table shopTable = builder.build();
        return shopTable;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = Style.getDefaultFont();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Hi " + backend.getGameState().party.player.name + ", what would you like to buy?",
                Style.MARGIN, Style.MARGIN + Style.FONT_SIZE);

        shopTable.draw(graphics, Style.MARGIN, Style.MARGIN + 3*Style.FONT_SIZE);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an item to buy or press [esc] to cancel.", Style.MARGIN, dim.height - Style.MARGIN);
    }
}
