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

    public ActionChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                              String message,
                              ItemList items,
                              int itemIndex,
                              List<ItemActions.Action> actions) {
        super(new WindowDim(x, y, 400,
                100 + 17 * actions.size()),
                true, backend, frontend);
        this.message = message;
        this.items = items;
        this.itemIndex = itemIndex;
        this.actions = actions;
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

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = Style.getDefaultFont();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(message, Style.SMALL_MARGIN, Style.SMALL_MARGIN + Style.getFontSize());

        DungeonItem item = items.get(itemIndex);

        TableBuilder tableBuilder = new TableBuilder();
        List<String> itemInfo = Arrays.asList(TextUtils.format(item.name, item.count, false),  item.bonusString());

        List<Cell> header = new ArrayList<>();
        header.add(new ImageCell(item.image, false));
        header.add(new TextCell(itemInfo, TextCell.Style.NORMAL, font));
        header.add(new EmptyCell());
        tableBuilder.addRow(header);

        List<Cell> spacer = new ArrayList<>();
        spacer.add(new EmptyCell(0, Style.SMALL_MARGIN));
        spacer.add(new EmptyCell(0, Style.SMALL_MARGIN));
        spacer.add(new EmptyCell(0, Style.SMALL_MARGIN));
        tableBuilder.addRow(spacer);

        int idx = 0;
        for (ItemActions.Action action : actions) {
            String label = (char) ((int) 'a' + idx) + ")";
            List<Cell> row = new ArrayList<>();
            row.add(new EmptyCell());
            row.add(new TextCell(Arrays.asList(label), TextCell.Style.NORMAL, font));
            row.add(new TextCell(Arrays.asList(action.getText()), TextCell.Style.NORMAL, font));
            idx++;
            tableBuilder.addRow(row);
        }

        tableBuilder.setColWidths(Arrays.asList(Style.TILE_SIZE, 30, 100));
        Table actionTable = tableBuilder.build();
        actionTable.draw(graphics, Style.SMALL_MARGIN, 30);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an action or press [esc] to cancel.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }
}
