package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.ShopUtils;
import pow.backend.action.UpgradeItem;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class JewelerShopWindow extends AbstractWindow {

    private static class IndexConverter {
        List<ShopUtils.ItemInfo> equipment;
        List<ShopUtils.ItemInfo> inventory;
        List<ShopUtils.ItemInfo> gems;

        private final int equipmentIdxMin;
        private final int inventoryIdxMin;
        private final int gemsIdxMin;

        public int equipmentSelect;
        public int inventorySelect;
        public int gemsSelect;

        public int size() {
            return equipment.size() + inventory.size() + gems.size();
        }

        public IndexConverter(List<ShopUtils.ItemInfo> equipment,
                              List<ShopUtils.ItemInfo> inventory,
                              List<ShopUtils.ItemInfo> gems) {
            this.equipment = equipment;
            this.inventory = inventory;
            this.gems = gems;

            this.equipmentIdxMin = 0;
            this.inventoryIdxMin = equipmentIdxMin + equipment.size();
            this.gemsIdxMin = inventoryIdxMin + inventory.size();

            if (equipment.isEmpty()) {
                equipmentSelect = -1;
                inventorySelect = 0;
            } else {
                equipmentSelect = 0;
                inventorySelect = -1;
            }
            gemsSelect = 0;
        }

        public void updateSelection(int idx) {
            if (equipmentIdxMin <= idx && idx < inventoryIdxMin) {
                equipmentSelect = idx - equipmentIdxMin;
                inventorySelect = -1;
            } else if (inventoryIdxMin <= idx && idx < gemsIdxMin) {
                equipmentSelect = -1;
                inventorySelect = idx - inventoryIdxMin;
            } else {
                gemsSelect = idx - gemsIdxMin;
            }
        }
    }

    private IndexConverter selections;
    private final Consumer<UpgradeItem.ItemIndices> callback;

    public JewelerShopWindow(WindowDim dim,
                             boolean visible,
                             GameBackend backend,
                             Frontend frontend,
                             Consumer<UpgradeItem.ItemIndices> callback) {
        super(dim, visible, backend, frontend);
        this.callback = callback;
        List<ShopUtils.ItemInfo> upgradeableEquipment = ShopUtils.getListOfUpgradeableItems(backend.getGameState().player.equipment);
        List<ShopUtils.ItemInfo> upgradeableInventory = ShopUtils.getListOfUpgradeableItems(backend.getGameState().player.inventory.items);
        List<ShopUtils.ItemInfo> gems = ShopUtils.getListOfGems(backend.getGameState().player);
        selections = new IndexConverter(upgradeableEquipment, upgradeableInventory, gems);
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
            if (itemNumber >= 0 && itemNumber < selections.size()) {
                selections.updateSelection(itemNumber);
                frontend.setDirty(true);
            }
        }

        if (keyCode == KeyEvent.VK_ENTER) {
            int equipmentIdx = selections.equipmentSelect < 0 ? -1 : selections.equipment.get(selections.equipmentSelect).listIndex;
            int inventoryIdx = selections.inventorySelect < 0 ? -1 : selections.inventory.get(selections.inventorySelect).listIndex;
            int gemsIdx = selections.gemsSelect < 0 ? -1 : selections.gems.get(selections.gemsSelect).listIndex;
            callback.accept(new UpgradeItem.ItemIndices(equipmentIdx, inventoryIdx, gemsIdx));
            frontend.close();
            return;
        }
    }

    final private int TILE_SIZE = 32;
    final private int MARGIN = 20;
    final private int FONT_SIZE = 12;

    private void drawItem(Graphics graphics, Point position, int idx, DungeonItem item, boolean isEnabled, boolean isSelected) {
        ImageController.DrawMode drawMode = isEnabled ? ImageController.DrawMode.NORMAL : ImageController.DrawMode.GRAY;
        ImageController.drawTile(graphics, item.image, position.x + 15, position.y, drawMode);

        String label = (char) ((int) 'a' + idx) + ")";
        int textY = position.y + 20;
        graphics.setColor(isEnabled ? (isSelected ? Color.YELLOW : Color.WHITE) : Color.GRAY);
        graphics.drawString(label, position.x, textY);
        graphics.drawString(item.stringWithInfo(), position.x + 50, textY);
    }

    @Override
    public void drawContents(Graphics graphics) {

        List<ShopUtils.ItemInfo> upgradeableEquipment = ShopUtils.getListOfUpgradeableItems(backend.getGameState().player.equipment);
        List<ShopUtils.ItemInfo> upgradeableInventory = ShopUtils.getListOfUpgradeableItems(backend.getGameState().player.inventory.items);
        List<ShopUtils.ItemInfo> gems = ShopUtils.getListOfGems(backend.getGameState().player);

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Hi " + backend.getGameState().player.name + ", select an item and a gem.",
                MARGIN, MARGIN + FONT_SIZE);

        final int yHeader = MARGIN + 3 * FONT_SIZE;
        final int col1x = MARGIN;
        final int col2x = 410;

        int y = yHeader;
        int idx = 0;

        // equipment, first column
        if (!upgradeableEquipment.isEmpty()) {
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawLine(col1x, y + 5, col2x - 30, y + 5);
            graphics.setColor(Color.WHITE);
            graphics.drawString("Equipment:", MARGIN, y);
            y += FONT_SIZE;
            for (ShopUtils.ItemInfo entry : upgradeableEquipment) {
                boolean isEnabled = true; // disable based on price
                boolean isSelected = idx == selections.equipmentSelect;
                drawItem(graphics, new Point(MARGIN, y), idx, entry.item, isEnabled, isSelected);
                idx++;
                y += TILE_SIZE;
            }

            y += 20;
        }

        // inventory, first column
        int baseInventoryIdx = idx;
        if (!upgradeableInventory.isEmpty()) {
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawLine(col1x, y + 5, col2x - 30, y + 5);
            graphics.setColor(Color.WHITE);
            graphics.drawString("Inventory:", MARGIN, y);
            y += FONT_SIZE;
            for (ShopUtils.ItemInfo entry : upgradeableInventory) {
                boolean isEnabled = true; // disable based on price
                boolean isSelected = idx - baseInventoryIdx == selections.inventorySelect;
                drawItem(graphics, new Point(MARGIN, y), idx, entry.item, isEnabled, isSelected);
                idx++;
                y += TILE_SIZE;
            }
        }

        // gems, second column
        int baseGemIdx = idx;
        y = yHeader;
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawLine(col2x, y + 5, dim.width - MARGIN, y + 5);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Gems:", col2x, y);
        y += FONT_SIZE;
        for (ShopUtils.ItemInfo entry : gems) {
            boolean isEnabled = true;
            boolean isSelected = idx - baseGemIdx == selections.gemsSelect;
            drawItem(graphics, new Point(col2x, y), idx, entry.item, isEnabled, isSelected);
            idx++;
            y += TILE_SIZE;
        }

        // status line at the bottom
        graphics.setColor(Color.WHITE);
        int cost = 5;
        graphics.drawString("It costs " + cost + " to combine", MARGIN, dim.height - 50);
        graphics.drawString("Press return to accept, esc to cancel.", MARGIN, dim.height - 38);
    }
}
