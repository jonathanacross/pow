package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameConstants;
import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.ItemList;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class DropPearl implements Action {
    private final Actor actor;
    private final Point tileLoc;

    public DropPearl(Actor actor, Point tileLoc) {
        this.actor = actor;
        this.tileLoc = tileLoc;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        GameMap map = backend.getGameState().getCurrentMap();
        DungeonSquare tile = map.map[tileLoc.x][tileLoc.y];


        int pearlIdx = findPearl(actor.inventory);
        boolean tileHasPearl = findPearl(tile.items) >= 0;

        List<GameEvent> events = new ArrayList<>();

        if (tileHasPearl) {
            backend.logMessage("There is already a pearl there.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.succeeded(events);
        }

        if (pearlIdx < 0) {
            backend.logMessage(actor.getNoun() + " has no pearl to drop there.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.succeeded(events);
        }

        // drop the pearl
        DungeonItem item = actor.inventory.items.get(pearlIdx);
        actor.inventory.items.remove(pearlIdx);
        tile.items.add(item);
        gs.party.numPearlsReturned++;
        backend.logMessage(actor.getNoun() + " returns a pearl!", MessageLog.MessageType.GAME_EVENT);

        if (!gs.party.player.winner && gs.party.numPearlsReturned >= GameConstants.NUM_PEARLS_TO_WIN) {
            gs.party.player.winner = true;
            backend.logMessage("Congratulations, you won!", MessageLog.MessageType.GAME_EVENT);
            events.add(GameEvent.WON_GAME);
        }

        events.add(GameEvent.DUNGEON_UPDATED);

        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }

    private int findPearl(ItemList list) {
        for (int i = 0; i < list.items.size(); i++) {
            if (list.items.get(i).flags.pearl) {
                return i;
            }
        }
        return -1;
    }
}
