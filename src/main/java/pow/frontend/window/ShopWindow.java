package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.ShopData;
import pow.backend.action.BuyItem;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
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
                    WindowDim cDim = new WindowDim(dim.x, dim.y, dim.width, 90);
                    frontend.open(new GetCountWindow(cDim, this.backend, this.frontend,
                            entry.item.image,
                            "How many " + TextUtils.plural(entry.item.name) + " do you want?",
                            maxNum,
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

    final private int TILE_SIZE = 32;
    final private int MARGIN = 20;
    final private int FONT_SIZE = 12;

    @Override
    public void drawContents(Graphics graphics) {

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Hi " + backend.getGameState().party.player.name + ", what would you like to buy?",
                MARGIN, MARGIN + FONT_SIZE);

        int priceX = dim.width - MARGIN - 40;
        graphics.drawString("Item", 20 + MARGIN, MARGIN + 3*FONT_SIZE);
        graphics.drawString("Price", priceX, MARGIN + 3*FONT_SIZE);

        graphics.setColor(Color.DARK_GRAY);
        graphics.drawLine(30, MARGIN + 3*FONT_SIZE + 5,
                dim.width - MARGIN, MARGIN + 3*FONT_SIZE + 5);

        int y = MARGIN + 4*FONT_SIZE;

        int idx = 0;
        for (ShopData.ShopEntry entry : this.entries) {
            boolean isEnabled = maxNumBuyable(entry) > 0;
            ImageController.DrawMode drawMode = isEnabled ? ImageController.DrawMode.NORMAL : ImageController.DrawMode.GRAY;
            ImageController.drawTile(graphics, entry.item.image, 15 + MARGIN, y, drawMode);

            String label = (char) ((int) 'a' + idx) + ")";
            int textY = y + 20;
            graphics.setColor(isEnabled ? Color.WHITE : Color.GRAY);
            graphics.drawString(label, MARGIN, textY);
            graphics.drawString(entry.item.stringWithInfo(), 50+MARGIN, textY);

            graphics.drawString(String.valueOf(entry.price), priceX, textY);

            idx++;
            y += TILE_SIZE;
        }
    }
}
