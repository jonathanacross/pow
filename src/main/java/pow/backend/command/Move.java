package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.event.GameEvent;
import pow.util.DebugLogger;

import javax.swing.Action;
import java.util.ArrayList;
import java.util.List;

public class Move implements CommandRequest {
    int dx;
    int dy;
    Actor actor;

    public Move(Actor actor, int dx, int dy) {
        this.actor = actor;
        this.dx = dx;
        this.dy = dy;
    }

    private List<GameEvent> addEvents(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.MOVED);
        GameState gs = backend.getGameState();
        DungeonFeature feature = gs.map.map[gs.player.x][gs.player.y].feature;

        // temporary custom code to demonstrate winning/losing
        if (actor == gs.player) {
            if (feature != null && feature.id.equals("wintile")) {
                backend.logMessage("you won!");
                events.add(GameEvent.WON_GAME);
            } else if (feature != null && feature.id.equals("losetile")) {
                backend.logMessage("you died.");
                events.add(GameEvent.LOST_GAME);
            }
        }
        return events;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();

        // just stay still
        if (dx == 0 && dy == 0) {
            return ActionResult.Succeeded(new ArrayList<>());
        }

        int newx = gs.player.x + dx;
        int newy = gs.player.y + dy;

        Actor defender = gs.map.actorAt(newx, newy);
        if (defender != null) {
            if (!defender.friendly)  {
                // attack
                return ActionResult.Failed(new Attack(gs.player, defender));
            }
            else {
                // friendly, swap positions
                return ActionResult.Failed(new Swap(gs.player, defender));
            }
        }

        if (! gs.map.isBlocked(newx, newy)) {
            // move
            gs.player.x = newx;
            gs.player.y = newy;
            return ActionResult.Succeeded(addEvents(backend));
        } else {
            // tried to move to a solid area
            return ActionResult.Failed(null);
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
