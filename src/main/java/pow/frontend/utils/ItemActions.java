package pow.frontend.utils;

import pow.backend.GameState;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class ItemActions {
    public enum ItemLocation {
        GROUND,
        INVENTORY,
        EQUIPMENT
    }

    public enum Action {
        GET("Get"),
        DROP("Drop"),
        WEAR("Wear"),
        TAKE_OFF("Take Off"),
        QUAFF("Quaff"),
        FIRE("Fire");

        private String text;
        Action(String text) {
            this.text = text;
        }

        public String getText() { return text; }
    }

    private static boolean canGet(ItemLocation location) {
        return location == ItemLocation.GROUND;
    }

    private static boolean canDrop(ItemLocation location) {
        return location != ItemLocation.GROUND;
    }

    private static boolean canWear(DungeonItem item, ItemLocation location) {
        return (location != ItemLocation.EQUIPMENT) &&
                (item.slot != DungeonItem.Slot.NONE);
    }

    private static boolean canTakeOff(ItemLocation location) {
        return location == ItemLocation.EQUIPMENT;
    }

    private static boolean canQuaff(DungeonItem item) {
        return item.flags.potion;
    }

    private static boolean canFire(DungeonItem item, GameState gameState) {
        if (!item.flags.arrow) {
            return false;
        }

        Player player = gameState.player;
        if (!player.hasBowEquipped()) {
            return false;
        }

        Point target = player.getTarget();
        if (target == null) {
            return false;
        }

        return true;
    }

    public static List<Action> getActions(DungeonItem item, GameState gameState, ItemLocation location) {
        List<Action> actions = new ArrayList<>();
        if (canGet(location)) {
            actions.add(Action.GET);
        }
        if (canQuaff(item)) {
            actions.add(Action.QUAFF);
        }
        if (canFire(item, gameState)) {
            actions.add(Action.FIRE);
        }
        if (canWear(item, location)) {
            actions.add(Action.WEAR);
        }
        if (canTakeOff(location)) {
            actions.add(Action.TAKE_OFF);
        }
        if (canDrop(location)) {
            actions.add(Action.DROP);
        }
        return actions;
    }

}
