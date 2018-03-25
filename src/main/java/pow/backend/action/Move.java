package pow.backend.action;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.DungeonExit;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Move implements Action {
    private final int dx;
    private final int dy;
    private final Actor actor;
    private final boolean pause;

    public Move(Actor actor, int dx, int dy) {
        this(actor, dx, dy, false);
    }

    public Move(Actor actor, int dx, int dy, boolean pause) {
        this.actor = actor;
        this.dx = dx;
        this.dy = dy;
        this.pause = pause;
    }

    private List<GameEvent> addEvents(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.Moved());
        if (pause) {
            events.add(GameEvent.Effect(new DungeonEffect(Collections.emptyList())));
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

        int newx = actor.loc.x + dx;
        int newy = actor.loc.y + dy;

        // Check that the target location is valid.  The ONLY case where it is
        // valid to move off the map is if the actor == player, and the 
        // player is on a tile that lets them go to another area.
        if (!gs.getCurrentMap().isOnMap(newx, newy)) {
            // check for player changing maps
            if (actor == gs.player) {
                DungeonTerrain currSquareTerrain = gs.getCurrentMap().map[actor.loc.x][actor.loc.y].terrain;
                if (currSquareTerrain.flags.teleport) {
                    DungeonExit exit = new DungeonExit(currSquareTerrain.actionParams.name);
                    String targetArea = exit.areaId;
                    Point targetLoc = gs.world.world.get(exit.areaId).keyLocations.get(exit.locName);
                    return ActionResult.Failed(new GotoArea(targetArea, targetLoc));
                }
            }

            backend.logMessage(actor.getPronoun() + " can't go that way", MessageLog.MessageType.USER_ERROR);
            return ActionResult.Failed(null);
        }

        // Check if we should attack
        Actor defender = gs.getCurrentMap().actorAt(newx, newy);
        if (defender != null) {
            if (!defender.friendly)  {
                // attack
                return ActionResult.Failed(new Attack(this.actor, defender));
            }
            else {
                // friendly, swap positions
                return ActionResult.Failed(new Swap(this.actor, defender));
            }
        }

        DungeonTerrain terrain = gs.getCurrentMap().map[newx][newy].terrain;
        if (terrain.flags.actOnStep) {
            if (!terrain.flags.diggable || actor.canDig()) {
                Point loc = new Point(newx, newy);
                ActionParams params = new ActionParams(terrain.actionParams);
                params.point = loc;
                Action newAction = ActionParams.buildAction(this.actor, params);
                return ActionResult.Failed(newAction);
            }
        }

        DungeonFeature feature = gs.getCurrentMap().map[newx][newy].feature;
        if (feature != null && feature.flags.actOnStep) {
            Point loc = new Point(newx, newy);
            ActionParams params = new ActionParams(feature.actionParams);
            params.point = loc;
            Action newAction = ActionParams.buildAction(this.actor, params);
            return ActionResult.Failed(newAction);
        }

        if (! gs.getCurrentMap().isBlocked(this.actor, newx, newy)) {
            // move
            actor.loc.x = newx;
            actor.loc.y = newy;
            if (actor == gs.player) {
                gs.getCurrentMap().updatePlayerVisibilityData(gs.player);
            }
            return ActionResult.Succeeded(addEvents(backend));
        } else {
            backend.logMessage(actor.getPronoun() + " can't go that way", MessageLog.MessageType.USER_ERROR);
            return ActionResult.Failed(null);
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
