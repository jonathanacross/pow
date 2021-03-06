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
import pow.frontend.utils.ItemActions;
import pow.frontend.widget.*;
import pow.util.TextUtils;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
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
                }
                frontend.close();
                frontend.close();
            }
        }
    }

    private Table getLayoutTable() {
        Font font = Style.getDefaultFont();

        // build the inner list
        Table list = new Table();
        for (int i = 0; i < actions.size(); i++) {
            ItemActions.Action action = actions.get(i);
            String label = (char) ((int) 'a' + i) + ")";
            list.addRow(Arrays.asList(
                    new TableCell(new TextBox(Collections.singletonList(label), State.NORMAL, font)),
                    new TableCell(new TextBox(Collections.singletonList(action.getText()), State.NORMAL, font))
            ));
        }
        list.setHSpacing(Style.MARGIN);
        list.autosize();

        // build the item line
        DungeonItem item = items.get(itemIndex);
        List<String> itemInfo = Arrays.asList(TextUtils.format(item.name, item.count, false),  item.bonusString());
        Table itemLine = new Table();
        itemLine.addRow(Arrays.asList(
                new TableCell(new Tile(item.image, State.NORMAL)),
                new TableCell(new TextBox(itemInfo, State.NORMAL, font))
        ));
        itemLine.setHSpacing(Style.MARGIN);
        itemLine.autosize();

        // build the overall layout
        Table layout = new Table();
        layout.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(message), State.NORMAL, font)),
                new TableCell(itemLine),
                new TableCell(list),
                new TableCell(new TextBox(Collections.singletonList("Select an action or press [esc] to cancel."), State.NORMAL, font))
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
