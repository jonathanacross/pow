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

public class TakeOff implements Action {
    private Player player;
    private int index;  // index in equipment

    public TakeOff(Player player, int index) {
        this.player = player;
        this.index = index;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        DungeonItem item = player.takeOff(index);
        backend.logMessage(player.getPronoun() + " take off " + TextUtils.format(item.name, 1, true));
        // put the item somewhere
        int numCanAdd = player.inventory.numCanAdd(item);
        if (numCanAdd == 0) {
            // put on the ground
            backend.logMessage(player.getPronoun() + " drop " + TextUtils.format(item.name, 1, false));
            gs.world.currentMap.map[player.loc.x][player.loc.y].items.add(item);
        } else {
            // save in inventory
            player.inventory.add(item);
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
