package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.*;
import pow.backend.actors.Player;
import pow.backend.ai.AutoItem;
import pow.backend.dungeon.ItemList;

import java.util.*;

// Class to optimize current equipment/inventory based on current holdings
// and what's on the ground.  Note that this is mostly designed for player
// optimization, as it only replaces equipment that is strictly better than
// current used equipment, rather than trying to find best global combinations
// of equipment for computer AI.
public class AutoItemBehavior implements Behavior {

    private final List<AutoItem.ItemMovement> plannedMovements;
    private final GameState gs;

    public AutoItemBehavior(GameState gs) {
        this.gs = gs;
        this.plannedMovements = AutoItem.simplifyMovements(AutoItem.optimizeItems(gs));
    }

    @Override
    public boolean canPerform(GameState gameState) {
        return !plannedMovements.isEmpty();
    }

    @Override
    public Action getAction() {
        return makeAction(plannedMovements.remove(0), gs);
    }

    private static Action makeAction(AutoItem.ItemMovement movement, GameState gs) {
        Player player = gs.party.player;
        ItemList groundItems = gs.getCurrentMap().map[player.loc.x][player.loc.y].items;
        ItemList inventoryItems = player.inventory;

        if (movement.from == AutoItem.Location.INVENTORY && movement.to == AutoItem.Location.GROUND) {
            int itemLoc = player.inventory.items.indexOf(movement.item);
            if (itemLoc >= 0) {
                return new Drop(player, itemLoc, movement.item.count);
            } else {
                System.out.println("error: couldn't find item " + movement.item.stringWithInfo() + " in player's inventory");
            }
        } else if (movement.from == AutoItem.Location.EQUIPMENT && movement.to == AutoItem.Location.GROUND) {
            int itemLoc = player.equipment.items.indexOf(movement.item);
            if (itemLoc >= 0) {
                return new DropEquipment(player, itemLoc);
            } else {
                System.out.println("error: couldn't find item " + movement.item.stringWithInfo() + " in player's equipment");
            }
        } else if (movement.from == AutoItem.Location.INVENTORY && movement.to == AutoItem.Location.EQUIPMENT) {
            int itemLoc = inventoryItems.items.indexOf(movement.item);
            if (itemLoc >= 0) {
                return new Wear(player, inventoryItems, itemLoc);
            } else {
                System.out.println("error: couldn't find item " + movement.item.stringWithInfo());
            }
        } else if (movement.from == AutoItem.Location.GROUND && movement.to == AutoItem.Location.EQUIPMENT) {
            int itemLoc = groundItems.items.indexOf(movement.item);
            if (itemLoc >= 0) {
                return new Wear(player, groundItems, itemLoc);
            } else {
                System.out.println("error: couldn't find item " + movement.item.stringWithInfo());
            }
        } else if (movement.from == AutoItem.Location.GROUND && movement.to == AutoItem.Location.INVENTORY) {
            int itemLoc = groundItems.items.indexOf(movement.item);
            if (itemLoc >= 0) {
                return new PickUp(player, itemLoc, movement.item.count);
            } else {
                System.out.println("error: couldn't find item " + movement.item.stringWithInfo() + " on the ground");
            }
        }

        // Shouldn't get here.
        System.out.println("warning: unexpected movement.");
        return null;
    }

}
