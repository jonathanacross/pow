package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.UpgradeItem;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.utils.ItemUtils;
import pow.backend.utils.ShopUtils;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class JewelerShopWindow extends AbstractWindow {

    private static class Selections {
        final List<ShopUtils.ItemInfo> equipment;
        final List<ShopUtils.ItemInfo> inventory;
        final List<ShopUtils.ItemInfo> gems;

        private final int playerGold;
        private final int equipmentIdxMin;
        private final int inventoryIdxMin;
        private final int gemsIdxMin;

        public int equipmentSelectIdx;
        public int inventorySelectIdx;
        public int gemsSelect;

        public int size() {
            return equipment.size() + inventory.size() + gems.size();
        }

        public boolean haveNeededItems() {
            return (equipment.size() + inventory.size() > 0) && (gems.size() > 0);
        }

        // Return equivalent item with count of 1, since we only transform
        // one item at a time.
        public DungeonItem getSelectedItem() {
            DungeonItem item = equipmentSelectIdx >= 0 ?
                    new DungeonItem(equipment.get(equipmentSelectIdx).item) :
                    new DungeonItem(inventory.get(inventorySelectIdx).item);
            item.count = 1;
            return item;
        }

        public DungeonItem getSelectedGem() {
            DungeonItem item = new DungeonItem(gems.get(gemsSelect).item);
            item.count = 1;
            return item;
        }

        public int getCostOfSelectedItems() {
            return ItemUtils.priceItem(getSelectedItem()) / 2 + ItemUtils.priceItem(getSelectedGem());
        }

        public boolean playerCanAffordUpgrade() {
            return getCostOfSelectedItems() <= playerGold;
        }

        public Selections(List<ShopUtils.ItemInfo> equipment,
                          List<ShopUtils.ItemInfo> inventory,
                          List<ShopUtils.ItemInfo> gems,
                          int playerGold) {
            this.equipment = equipment;
            this.inventory = inventory;
            this.gems = gems;
            this.playerGold = playerGold;

            this.equipmentIdxMin = 0;
            this.inventoryIdxMin = equipmentIdxMin + equipment.size();
            this.gemsIdxMin = inventoryIdxMin + inventory.size();

            if (!equipment.isEmpty()) {
                equipmentSelectIdx = 0;
                inventorySelectIdx = -1;
            } else if (!inventory.isEmpty()) {
                equipmentSelectIdx = -1;
                inventorySelectIdx = 0;
            } else {
                equipmentSelectIdx = -1;
                inventorySelectIdx = -1;
            }

            if (!gems.isEmpty()) {
                gemsSelect = 0;
            } else {
                gemsSelect = -1;
            }
        }

        public void updateSelection(int idx) {
            if (equipmentIdxMin <= idx && idx < inventoryIdxMin) {
                equipmentSelectIdx = idx - equipmentIdxMin;
                inventorySelectIdx = -1;
            } else if (inventoryIdxMin <= idx && idx < gemsIdxMin) {
                equipmentSelectIdx = -1;
                inventorySelectIdx = idx - inventoryIdxMin;
            } else {
                gemsSelect = idx - gemsIdxMin;
            }
        }
    }

    private final Selections selections;
    private final Consumer<UpgradeItem.UpgradeInfo> callback;

    public JewelerShopWindow(WindowDim dim,
                             boolean visible,
                             GameBackend backend,
                             Frontend frontend,
                             Consumer<UpgradeItem.UpgradeInfo> callback) {
        super(dim, visible, backend, frontend);
        this.callback = callback;
        Player player = backend.getGameState().party.player;
        List<ShopUtils.ItemInfo> equipment = ShopUtils.getListOfUpgradeableItems(player.equipment.items);
        List<ShopUtils.ItemInfo> inventory = ShopUtils.getListOfUpgradeableItems(player.inventory.items);
        List<ShopUtils.ItemInfo> gems = ShopUtils.getListOfGems(player);
        selections = new Selections(equipment, inventory, gems, player.gold);
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            frontend.close();
            return;
        }
        if (selections.haveNeededItems()) {
            if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
                int itemNumber = keyCode - KeyEvent.VK_A;
                if (itemNumber >= 0 && itemNumber < selections.size()) {
                    selections.updateSelection(itemNumber);
                    frontend.setDirty(true);
                }
            }

            if (!selections.playerCanAffordUpgrade()) return;
            if (keyCode == KeyEvent.VK_ENTER) {
                int equipmentIdx = getIdxOrMinus1(selections.equipment, selections.equipmentSelectIdx);
                int inventoryIdx = getIdxOrMinus1(selections.inventory, selections.inventorySelectIdx);
                int gemsIdx = getIdxOrMinus1(selections.gems, selections.gemsSelect);
                callback.accept(new UpgradeItem.UpgradeInfo(equipmentIdx, inventoryIdx, gemsIdx, selections.getCostOfSelectedItems()));
                frontend.close();
            }
        }
    }

    private static int getIdxOrMinus1(List<ShopUtils.ItemInfo> itemInfoList, int idx) {
        return idx < 0 ? -1 : itemInfoList.get(idx).listIndex;
    }

    final private int TILE_SIZE = 32;
    final private int MARGIN = 20;
    final private int FONT_SIZE = 12;

    private void drawItem(Graphics graphics, Point position, int idx, DungeonItem item, boolean isSelected, boolean isEnabled) {
        ImageController.DrawMode drawMode =
                isEnabled ? ImageController.DrawMode.NORMAL : ImageController.DrawMode.GRAY;
        ImageController.drawTile(graphics, item.image, position.x + 15, position.y, drawMode);

        graphics.setColor(isEnabled ? (isSelected ? Color.YELLOW : Color.WHITE) : Color.GRAY);
        int textY = position.y + 20;
        if (idx >= 0) {
            String label = (char) ((int) 'a' + idx) + ")";
            graphics.drawString(label, position.x, textY);
        }
        graphics.drawString(item.stringWithInfo(), position.x + 50, textY);
    }

    @Override
    public void drawContents(Graphics graphics) {

        Player player = backend.getGameState().party.player;

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);


        // if insufficient items, then let the player know and skip the complex drawing logic.
        if (!selections.haveNeededItems()) {
            graphics.drawString("Hi " + player.name + ", come back when you have a socketed item and a gem.",
                    MARGIN, MARGIN + FONT_SIZE);

            graphics.drawString("Press Esc to exit.", MARGIN, dim.height - MARGIN);
            return;
        }

        graphics.drawString("Hi " + player.name + ", select an item and a gem.",
                MARGIN, MARGIN + FONT_SIZE);

        final int yHeader = MARGIN + 3 * FONT_SIZE;
        final int col1x = MARGIN;
        final int col2x = 430;

        int y = yHeader;
        int idx = 0;

        // equipment, first column
        if (!selections.equipment.isEmpty()) {
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawLine(col1x, y + 5, col2x - 30, y + 5);
            graphics.setColor(Color.WHITE);
            graphics.drawString("Equipment:", MARGIN, y);
            y += FONT_SIZE;
            for (ShopUtils.ItemInfo entry : selections.equipment) {
                boolean isSelected = idx == selections.equipmentSelectIdx;
                drawItem(graphics, new Point(MARGIN, y), idx, entry.item, isSelected, true);
                idx++;
                y += TILE_SIZE;
            }

            y += 20;
        }

        // inventory, first column
        int baseInventoryIdx = idx;
        if (!selections.inventory.isEmpty()) {
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawLine(col1x, y + 5, col2x - 30, y + 5);
            graphics.setColor(Color.WHITE);
            graphics.drawString("Inventory:", MARGIN, y);
            y += FONT_SIZE;
            for (ShopUtils.ItemInfo entry : selections.inventory) {
                boolean isSelected = idx - baseInventoryIdx == selections.inventorySelectIdx;
                drawItem(graphics, new Point(MARGIN, y), idx, entry.item, isSelected, true);
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
        for (ShopUtils.ItemInfo entry : selections.gems) {
            boolean isSelected = idx - baseGemIdx == selections.gemsSelect;
            drawItem(graphics, new Point(col2x, y), idx, entry.item, isSelected, true);
            idx++;
            y += TILE_SIZE;
        }

        // status at the bottom
        y = dim.height - 125;
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawLine(MARGIN, y + 5, dim.width - MARGIN, y + 5);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Items to combine:", MARGIN, y);

        DungeonItem upgradeItem = selections.getSelectedItem();
        DungeonItem gem = selections.getSelectedGem();
        boolean enabled = selections.playerCanAffordUpgrade();

        y += FONT_SIZE;
        drawItem(graphics, new Point(col1x, y), -1, upgradeItem, false, enabled);
        drawItem(graphics, new Point(col2x, y), -1, gem, false, enabled);

        y += TILE_SIZE + 2 * FONT_SIZE;
        graphics.setColor(Color.WHITE);
        graphics.drawString("It costs " + selections.getCostOfSelectedItems() +
                " gold to combine these items.", MARGIN, y);
        y += FONT_SIZE;
        if (selections.playerCanAffordUpgrade()) {
            y += 2 * FONT_SIZE;
            graphics.drawString("Press return to accept, Esc to cancel.", MARGIN, y);
        } else {
            graphics.setColor(Color.RED);
            graphics.drawString("You don't have enough money.", MARGIN, y);
            y += 2 * FONT_SIZE;
            graphics.setColor(Color.WHITE);
            graphics.drawString("Choose another combination or press Esc to cancel.", MARGIN, y);

        }
    }
}
