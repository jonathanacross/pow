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
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ShopWindow extends AbstractWindow {
    private final List<ShopData.ShopEntry> entries;

    public ShopWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend,
                      List<ShopData.ShopEntry> entries) {
        super(dim, visible, backend, frontend);
        this.entries = entries;
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

    @Override
    public void drawContents(Graphics graphics) {

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);
        graphics.drawString("Hi " + backend.getGameState().party.player.name + ", what would you like to buy?",
                Style.MARGIN, Style.MARGIN + Style.FONT_SIZE);

        int priceX = dim.width - Style.MARGIN - 40;
        graphics.drawString("Item", 20 + Style.MARGIN, Style.MARGIN + 3*Style.FONT_SIZE);
        graphics.drawString("Price", priceX, Style.MARGIN + 3*Style.FONT_SIZE);

        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        graphics.drawLine(30, Style.MARGIN + 3*Style.FONT_SIZE + 5,
                dim.width - Style.MARGIN, Style.MARGIN + 3*Style.FONT_SIZE + 5);

        int y = Style.MARGIN + 4*Style.FONT_SIZE;

        int idx = 0;
        for (ShopData.ShopEntry entry : this.entries) {
            boolean isEnabled = maxNumBuyable(entry) > 0;
            ImageController.DrawMode drawMode = isEnabled ? ImageController.DrawMode.NORMAL : ImageController.DrawMode.GRAY;
            ImageController.drawTile(graphics, entry.item.image, 15 + Style.MARGIN, y, drawMode);

            String label = (char) ((int) 'a' + idx) + ")";
            int textY = y + 20;
            graphics.setColor(isEnabled ? Color.WHITE : Color.GRAY);
            graphics.drawString(label, Style.MARGIN, textY);
            graphics.drawString(TextUtils.format(entry.item.name, entry.item.count, false),  50+Style.MARGIN, textY - 5);
            graphics.drawString(entry.item.bonusString(), 50+Style.MARGIN, textY + Style.FONT_SIZE - 5);

            graphics.drawString(String.valueOf(entry.price), priceX, textY);

            idx++;
            y += Style.TILE_SIZE;
        }

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an item to buy or press [esc] to cancel.", Style.MARGIN, dim.height - Style.MARGIN);
    }
}
