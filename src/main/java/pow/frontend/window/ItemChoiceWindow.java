package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemChoiceWindow extends AbstractWindow {

    public static class ItemChoice {
        public final boolean useSecondList;
        public final int itemIdx;

        public ItemChoice(boolean useSecondList, int itemIdx) {
            this.useSecondList = useSecondList;
            this.itemIdx = itemIdx;
        }
    }

    private final String message;
    private final String altMessage;
    // TODO: change to take itemLists instead of List<DungeonItem>
    private final List<DungeonItem> items;
    private final List<DungeonItem> altItems;
    private boolean useSecondList;
    private final Function<DungeonItem, Boolean> enabled;
    private final Consumer<ItemChoice> callback;

    public ItemChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                            String message,
                            String altMessage,
                            List<DungeonItem> items,
                            List<DungeonItem> altItems,
                            Function<DungeonItem, Boolean> enabled,
                            Consumer<ItemChoice> callback) {
        super(new WindowDim(x, y, 400,
                60 + 32 * Math.max(items.size(), altItems == null ? 0 : altItems.size())),
                true, backend, frontend);
        this.message = message;
        this.altMessage = altMessage;
        this.items = items;
        this.altItems = altItems;
        this.enabled = enabled;
        this.callback = callback;
        this.useSecondList = false;
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            frontend.close();
            return;
        }

        if (keyCode == KeyEvent.VK_TAB && this.altItems != null) {
            useSecondList = !useSecondList;
            frontend.setDirty(true);
            return;
        }

        List<DungeonItem> currItems = useSecondList ? altItems : items;
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            int itemNumber = keyCode - KeyEvent.VK_A;
            if (itemNumber >= 0 && itemNumber < currItems.size() &&
                    enabled.apply(currItems.get(itemNumber))) {
                this.callback.accept(new ItemChoice(useSecondList, itemNumber));
                frontend.close();
            }
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        String currMessage = this.useSecondList ? this.altMessage : this.message;
        List<DungeonItem> currItems = this.useSecondList ? this.altItems : this.items;

        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        graphics.drawString(currMessage, Style.SMALL_MARGIN, Style.SMALL_MARGIN + Style.getFontSize());

        int y = 30;
        int idx = 0;
        for (DungeonItem item : currItems) {
            boolean isEnabled = enabled.apply(item);
            ImageController.DrawMode drawMode = isEnabled ? ImageController.DrawMode.NORMAL : ImageController.DrawMode.GRAY;
            ImageController.drawTile(graphics, item.image, 25, y, drawMode);

            String label = (char) ((int) 'a' + idx) + ")";
            int textY = y + 20;
            graphics.setColor(isEnabled ? Color.WHITE : Color.GRAY);
            graphics.drawString(label, Style.SMALL_MARGIN, textY);
            graphics.drawString(TextUtils.format(item.name, item.count, false),  60, textY - 5);
            graphics.drawString(item.bonusString(), 60, textY + Style.getFontSize() - 5);

            idx++;
            y += Style.TILE_SIZE;
        }

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an item, or press [esc] to cancel.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }
}
