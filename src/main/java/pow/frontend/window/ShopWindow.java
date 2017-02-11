package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class ShopWindow extends AbstractWindow {
    private List<ShopData.ShopEntry> items;

    public ShopWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend,
                      List<ShopData.ShopEntry> items) {
        super(dim, visible, backend, frontend);
        this.items = items;
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
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
        graphics.drawString("Hi " + backend.getGameState().player.name + ", what would you like to buy?",
                MARGIN, MARGIN + FONT_SIZE);

        int priceX = dim.width - MARGIN - 40;
        graphics.drawString("Item", 20 + MARGIN, MARGIN + 3*FONT_SIZE);
        graphics.drawString("Price", priceX, MARGIN + 3*FONT_SIZE);

        graphics.setColor(Color.DARK_GRAY);
        graphics.drawLine(30, MARGIN + 3*FONT_SIZE + 5,
                dim.width - MARGIN, MARGIN + 3*FONT_SIZE + 5);

        int y = MARGIN + 4*FONT_SIZE;

        int idx = 0;
        for (ShopData.ShopEntry entry : this.items) {
            boolean isEnabled = entry.price <= backend.getGameState().player.gold;
            ImageController.drawTile(graphics, entry.item.image, 15 + MARGIN, y, !isEnabled);

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
