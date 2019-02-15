package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.*;
import pow.backend.actors.Player;
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

// Window to choose which action to take (given a particular item).
public class ActionChoiceWindow extends AbstractWindow {

    private final String message;
    private final ItemList items;
    private final int itemIndex;
    private final List<ItemActions.Action> actions;
    private final Table layoutTable;

    public ActionChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                              String message,
                              ItemList items,
                              int itemIndex,
                              List<ItemActions.Action> actions) {
        super(new WindowDim(0, 0, 0, 0), true, backend, frontend);
        this.message = message;
        this.items = items;
        this.itemIndex = itemIndex;
        this.actions = actions;
        this.layoutTable = getLayoutTable();
        this.resize(new WindowDim(x, y, layoutTable.getWidth() + 2*Style.MARGIN,
                layoutTable.getHeight() + 2*Style.MARGIN));
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            frontend.close();
            return;
        }

        GameState gs = backend.getGameState();
        Player selectedActor = gs.party.selectedActor;
        Player pet = gs.party.pet;
        DungeonItem item = items.get(itemIndex);

        // TODO: change so can use f/q/d/g/w/W as alternates
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            int actionNumber = keyCode - KeyEvent.VK_A;
            if (actionNumber < actions.size()) {
                ItemActions.Action action = actions.get(actionNumber);
                switch (action) {
                    case GET:
                        backend.tellSelectedActor(new PickUp(selectedActor, itemIndex, item.count));
                        break;
                    case DROP:
                        backend.tellSelectedActor(new Drop(selectedActor, itemIndex, item.count));
                        break;
                    case DROP_EQUIPMENT:
                        backend.tellSelectedActor(new DropEquipment(selectedActor, itemIndex));
                        break;
                    case FIRE:
                        backend.tellSelectedActor(new Arrow(selectedActor, selectedActor.getTarget(), selectedActor.getSecondaryAttack()));
                        break;
                    case WEAR:
                        backend.tellSelectedActor(new Wear(selectedActor, items, itemIndex));
                        break;
                    case QUAFF:
                        backend.tellSelectedActor(new Quaff(selectedActor, items, itemIndex));
                        break;
                    case FEED:
                        backend.tellSelectedActor(new FeedPet(selectedActor, pet, items, itemIndex));
                        break;
                    case TAKE_OFF:
                        backend.tellSelectedActor(new TakeOff(selectedActor, itemIndex));
                        break;
//                    case GIVE:
//                        // TODO: allow user to specify count
//                        backend.tellSelectedActor(new TransferItem(selectedActor, nonselectedActor, itemIndex, item.count));
                }
                frontend.close();
                frontend.close();
            }
        }
    }

    private Table getLayoutTable() {
        Font font = Style.getDefaultFont();

        // build the inner list
        TableBuilder listBuilder = new TableBuilder();
        for (int i = 0; i < actions.size(); i++) {
            ItemActions.Action action = actions.get(i);
            String label = (char) ((int) 'a' + i) + ")";
            listBuilder.addRow(Arrays.asList(
                    new TextCell(Arrays.asList(label), TextCell.Style.NORMAL, font),
                    new TextCell(Arrays.asList(action.getText()), TextCell.Style.NORMAL, font)
            ));
        }
        listBuilder.setHSpacing(Style.MARGIN);
        Table list = listBuilder.build();

        // build the item line
        DungeonItem item = items.get(itemIndex);
        List<String> itemInfo = Arrays.asList(TextUtils.format(item.name, item.count, false),  item.bonusString());
        TableBuilder itemLineBuilder = new TableBuilder();
        itemLineBuilder.addRow(Arrays.asList(
                new ImageCell(item.image, false),
                new TextCell(itemInfo, TextCell.Style.NORMAL, font)
        ));
        itemLineBuilder.setHSpacing(Style.MARGIN);
        Table itemLine = itemLineBuilder.build();

        // build the overall layout
        TableBuilder builder = new TableBuilder();
        builder.addRow(Arrays.asList(
                new TextCell(Arrays.asList(message), TextCell.Style.NORMAL, font)
        ));
        builder.addRow(Arrays.asList(
                itemLine
        ));
        builder.addRow(Arrays.asList(
                list
        ));
        builder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("Select an action or press [esc] to cancel."), TextCell.Style.NORMAL, font)
        ));
        builder.setVSpacing(Style.MARGIN);

        return builder.build();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        layoutTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
