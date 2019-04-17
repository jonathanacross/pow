package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.UpgradeItem;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.utils.ItemUtils;
import pow.backend.utils.ShopUtils;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.widget.*;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
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
        public int gemsSelectIdx;

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
            DungeonItem item = new DungeonItem(gems.get(gemsSelectIdx).item);
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
                gemsSelectIdx = 0;
            } else {
                gemsSelectIdx = -1;
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
                gemsSelectIdx = idx - gemsIdxMin;
            }
        }
    }

    private final Selections selections;
    private final Consumer<UpgradeItem.UpgradeInfo> callback;

    public JewelerShopWindow(boolean visible,
                             GameBackend backend,
                             Frontend frontend,
                             Consumer<UpgradeItem.UpgradeInfo> callback) {
        super(new WindowDim(0, 0, 0, 0), visible, backend, frontend);
        this.callback = callback;
        Player player = backend.getGameState().party.player;
        List<ShopUtils.ItemInfo> equipment = ShopUtils.getListOfUpgradeableItems(player.equipment.items);
        List<ShopUtils.ItemInfo> inventory = ShopUtils.getListOfUpgradeableItems(player.inventory.items);
        List<ShopUtils.ItemInfo> gems = ShopUtils.getListOfGems(player);
        selections = new Selections(equipment, inventory, gems, player.gold);

        Table layoutTable = getLayoutTable();
        int height = 2*Style.MARGIN + layoutTable.getHeight();
        int width = 2*Style.MARGIN + layoutTable.getWidth();
        this.resize(frontend.layout.center(width, height));
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
                int gemsIdx = getIdxOrMinus1(selections.gems, selections.gemsSelectIdx);
                callback.accept(new UpgradeItem.UpgradeInfo(equipmentIdx, inventoryIdx, gemsIdx, selections.getCostOfSelectedItems()));
                frontend.close();
            }
        }
    }

    private static int getIdxOrMinus1(List<ShopUtils.ItemInfo> itemInfoList, int idx) {
        return idx < 0 ? -1 : itemInfoList.get(idx).listIndex;
    }

    public Table getItemTable(String title, int labelStartIdx, int selectIdx, List<ShopUtils.ItemInfo> items) {
        Font font = Style.getDefaultFont();

        Table itemList = new Table();
        for (int idx = 0; idx < items.size(); idx++) {
            DungeonItem item = items.get(idx).item;
            State state = idx == selectIdx ? State.SELECTED : State.NORMAL;
            String label = (char) ((int) 'a' + idx + labelStartIdx) + ")";
            List<String> itemInfo = Arrays.asList(TextUtils.format(item.name, item.count, false),  item.bonusString());
            itemList.addRow(Arrays.asList(
                    new TableCell(new TextBox(Collections.singletonList(label), state, font)),
                    new TableCell(new Tile(item.image, state)),
                    new TableCell(new TextBox(itemInfo, state, font))
            ));
        }
        itemList.setHSpacing(Style.MARGIN);
        itemList.autosize();

        Table table = new Table();
        table.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(title), State.NORMAL, font)),
                new TableCell(itemList)
                ));
        table.setVSpacing(Style.MARGIN);
        table.setDrawHeaderLine(true);
        table.autosize();

        return table;
    }

    public Table getLayoutTable() {
        Player player = backend.getGameState().party.player;
        Font font = Style.getDefaultFont();

        // if insufficient items, then let the player know and skip the complex drawing logic.
        if (!selections.haveNeededItems()) {
            String greeting = "Hi " + player.name + ", come back when you have a socketed item and a gem.";
            String helpMsg = "Press [esc] to exit.";
            Table table = new Table();
            table.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(greeting), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(helpMsg), State.NORMAL, font))
            ));
            table.setVSpacing(Style.MARGIN);
            table.autosize();
            return table;
        }

        // left pane
        int labelStartIdx = 0;
        Table leftPane = new Table();
        if (!selections.equipment.isEmpty()) {
            leftPane.addRow(Collections.singletonList(
                    new TableCell(getItemTable("Equipment:", labelStartIdx, selections.equipmentSelectIdx,
                            selections.equipment))
            ));
            labelStartIdx += selections.equipment.size();
        }
        if (!selections.inventory.isEmpty()) {
            leftPane.addRow(Collections.singletonList(
                    new TableCell(getItemTable("Inventory:", labelStartIdx, selections.inventorySelectIdx,
                            selections.inventory))
            ));
            labelStartIdx += selections.inventory.size();
        }
        leftPane.setVSpacing(Style.MARGIN);
        leftPane.autosize();

        // right pane
        Table rightPane = new Table();
        rightPane.addRow(Collections.singletonList(
                new TableCell(getItemTable("Gems:", labelStartIdx, selections.gemsSelectIdx,
                        selections.gems))
        ));
        rightPane.autosize();

        // select table
        Table selectTable = new Table();
        selectTable.addRow(Arrays.asList(
                new TableCell(leftPane, TableCell.VertAlign.TOP, TableCell.HorizAlign.LEFT),
                new TableCell(rightPane, TableCell.VertAlign.TOP, TableCell.HorizAlign.LEFT)
        ));
        selectTable.setHSpacing(Style.MARGIN);
        selectTable.autosize();

        // full layout
        String title = "Hi " + player.name + ", select an item and a gem.";
        String costMsg = "It costs " + selections.getCostOfSelectedItems() +
                " gold to combine these items.";
        String errMsg = selections.playerCanAffordUpgrade()
                ? ""
                : "You don't have enough money.";
        String helpMsg = selections.playerCanAffordUpgrade()
                ? "Press return to accept, Esc to cancel."
                : "Choose another combination or press [esc] to cancel.";


        Table costTable = new Table();
        costTable.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(costMsg), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(errMsg), State.ERROR, font))
        ));
        costTable.autosize();

        Table layout = new Table();
        layout.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(title), State.NORMAL, font)),
                new TableCell(selectTable),
                new TableCell(costTable),
                new TableCell(new TextBox(Collections.singletonList(helpMsg), State.NORMAL, font))
        ));
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();

        return layout;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Table layout = getLayoutTable();
        layout.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
