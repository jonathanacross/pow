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
        GET,
        DROP,
        WEAR,
        TAKE_OFF,
        QUAFF,
        FIRE
    }

    private static boolean canGet(DungeonItem item, GameState gameState, ItemLocation location) {
        return location == ItemLocation.GROUND;
    }

    private static boolean canDrop(DungeonItem item, GameState gameState, ItemLocation location) {
        return location != ItemLocation.GROUND;
    }

    private static boolean canWear(DungeonItem item, GameState gameState, ItemLocation location) {
        return (location != ItemLocation.EQUIPMENT) &&
                (item.slot != DungeonItem.Slot.NONE);
    }

    private static boolean canTakeOff(DungeonItem item, GameState gameState, ItemLocation location) {
        return location == ItemLocation.EQUIPMENT;
    }

    private static boolean canQuaff(DungeonItem item, GameState gameState, ItemLocation location) {
        return item.flags.potion;
    }

    private static boolean canFire(DungeonItem item, GameState gameState, ItemLocation location) {
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

    public static List<Action> GetActions(DungeonItem item, GameState gameState, ItemLocation location) {
        List<Action> actions = new ArrayList<>();
        if (canGet(item, gameState, location)) {
            actions.add(Action.GET);
        }
        if (canQuaff(item, gameState, location)) {
            actions.add(Action.QUAFF);
        }
        if (canFire(item, gameState, location)) {
            actions.add(Action.FIRE);
        }
        if (canWear(item, gameState, location)) {
            actions.add(Action.WEAR);
        }
        if (canTakeOff(item, gameState, location)) {
            actions.add(Action.TAKE_OFF);
        }
        if (canDrop(item, gameState, location)) {
            actions.add(Action.DROP);
        }

        return actions;
    }

}
