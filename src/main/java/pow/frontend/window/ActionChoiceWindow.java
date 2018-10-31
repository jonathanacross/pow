package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.*;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.ItemList;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.ItemActions;
import pow.util.Point;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

// Window to choose which action to take (given a particular item).
public class ActionChoiceWindow extends AbstractWindow {

    private final String message;
    private final ItemList items;
    private final int itemIndex;
    private final ItemActions.ItemLocation location;
    private final List<ItemActions.Action> actions;

    public ActionChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                              String message,
                              ItemList items,
                              int itemIndex,
                              ItemActions.ItemLocation location,
                              List<ItemActions.Action> actions) {
        super(new WindowDim(x, y, 320,
                80 + 17 * actions.size()),
                true, backend, frontend);
        this.message = message;
        this.items = items;
        this.itemIndex = itemIndex;
        this.location = location;
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
        Player player = gs.player;
        Pet pet = gs.pet;
        DungeonItem item = items.get(itemIndex);

        // TODO: change so can use f/q/d/g/w/W as alternates
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            int actionNumber = keyCode - KeyEvent.VK_A;
            if (actionNumber >= 0 && actionNumber < actions.size()) {
                ItemActions.Action action = actions.get(actionNumber);
                switch (action) {
                    case GET:
                        backend.tellPlayer(new PickUp(player, itemIndex, item.count));
                        break;
                    case DROP:
                        if (location != ItemActions.ItemLocation.PET) {
                            backend.tellPlayer(new Drop(player, itemIndex, item.count));
                        } else {
                            backend.tellPet(new Drop(pet, itemIndex, item.count));
                        }
                        break;
                    case FIRE:
                        backend.tellPlayer(new Arrow(player, player.getTarget(), player.getSecondaryAttack()));
                        break;
                    case WEAR:
                        backend.tellPlayer(new Wear(player, items, itemIndex));
                        break;
                    case QUAFF:
                        if (location != ItemActions.ItemLocation.PET) {
                            backend.tellPlayer(new Quaff(player, items, itemIndex));
                        } else {
                            backend.tellPet(new Quaff(pet, items, itemIndex));
                        }
                        break;
                    case TAKE_OFF:
                        backend.tellPlayer(new TakeOff(player, itemIndex));
                        break;
                    case GIVE:
                        if (location != ItemActions.ItemLocation.PET) {
                            // TODO: allow user to specify count
                            backend.tellPlayer(new TransferItem(player, pet, itemIndex, item.count));
                        } else {
                            backend.tellPet(new TransferItem(pet, player, itemIndex, item.count));
                        }
                }
                frontend.close();
                frontend.close();
            }
        }
    }

    final private int MARGIN = 10;
    final private int FONT_SIZE = 12;
    final private int FONT_SPACING = FONT_SIZE + 5;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(message, MARGIN, MARGIN + FONT_SIZE);

        int y = 30;
        int idx = 0;
        DungeonItem item = items.get(itemIndex);
        ImageController.drawTile(graphics, item.image, MARGIN, y);
        graphics.drawString(TextUtils.format(item.name, item.count, false),  MARGIN + 40, y + FONT_SIZE + 2);
        graphics.drawString(item.bonusString(), MARGIN + 40, y + 2*FONT_SIZE + 2);

        y = 80;
        for (ItemActions.Action action : actions) {
            String label = (char) ((int) 'a' + idx) + ")";
            y = 80 + FONT_SPACING * idx;
            graphics.drawString(label, MARGIN, y);
            graphics.drawString(action.getText(), 60, y);
            idx++;
        }
    }
}
