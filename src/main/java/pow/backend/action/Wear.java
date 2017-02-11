package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Wear implements Action {
    private Player player;
    private int index;  // index in inventory

    public Wear(Player player, int index) {
        this.player = player;
        this.index = index;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        DungeonItem item = player.inventory.items.get(index);
        player.inventory.removeOneItemAt(index);
        backend.logMessage(player.getPronoun() + " wear " + TextUtils.format(item.name, 1, true));
        DungeonItem oldItem = player.wear(item);
        // put the old item somewhere
        if (oldItem != null) {
            int numCanAdd = player.inventory.numCanAdd(oldItem);
            if (numCanAdd == 0) {
                // put on the ground
                backend.logMessage(player.getPronoun() + " drop " + TextUtils.format(item.name, 1, false));
                gs.getCurrentMap().map[player.loc.x][player.loc.y].items.add(oldItem);
            } else {
                // save in inventory
                player.inventory.add(oldItem);
            }
        }

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }

    @Override
    public Actor getActor() { return player; }
}
