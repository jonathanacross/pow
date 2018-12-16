package pow.backend.action;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.ActionParams;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.DungeonExit;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;
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
        events.add(GameEventOld.Moved());
        if (pause) {
            events.add(GameEventOld.Effect(new DungeonEffect(Collections.emptyList())));
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
            return ActionResult.Succeeded(addEvents(backend));
        }

        int newx = actor.loc.x + dx;
        int newy = actor.loc.y + dy;

        // Check that the target location is valid.  The ONLY case where it is
        // valid to move off the map is if the actor == player, and the 
        // player is on a tile that lets them go to another area.
        if (!gs.getCurrentMap().isOnMap(newx, newy)) {
            // check for player changing maps
            if (gs.party.containsActor(actor)) {
                DungeonTerrain currSquareTerrain = gs.getCurrentMap().map[actor.loc.x][actor.loc.y].terrain;
                if (currSquareTerrain.flags.teleport) {
                    DungeonExit exit = new DungeonExit(currSquareTerrain.actionParams.name);
                    String targetArea = exit.areaId;
                    Point targetLoc = gs.world.world.get(exit.areaId).keyLocations.get(exit.locName);
                    return ActionResult.Failed(new GotoArea(targetArea, targetLoc));
                }
            }

            backend.logMessage(actor.getNoun() + " can't go that way", MessageLog.MessageType.DEBUG);
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
            if (feature.flags.stairsDown || feature.flags.stairsUp) {
                // special case if the feature is stairs, as we have to compute final destination
                // location based on the direction of the player.
                DungeonExit exit = new DungeonExit(feature.actionParams.name);
                String targetArea = exit.areaId;
                Point targetLoc = gs.world.world.get(exit.areaId).keyLocations.get(exit.locName);
                Point adjustedTargetLoc = new Point(targetLoc.x + dx, targetLoc.y + dy);
                return ActionResult.Failed(new GotoArea(targetArea, adjustedTargetLoc));
            } else {
                Point loc = new Point(newx, newy);
                ActionParams params = new ActionParams(feature.actionParams);
                params.point = loc;
                Action newAction = ActionParams.buildAction(this.actor, params);
                return ActionResult.Failed(newAction);
            }
        }

        if (! gs.getCurrentMap().isBlocked(this.actor, newx, newy)) {
            // move
            actor.loc.x = newx;
            actor.loc.y = newy;
            if (actor == gs.party.selectedActor) {
                gs.getCurrentMap().updatePlayerVisibilityData(gs.party.player, gs.party.pet);
            }
            for (Player p : gs.party.playersInParty()) {
                // update the party's targeting, in case the player or target move
                // out of visibility range.
                p.target.update(gs, p);
            }
            return ActionResult.Succeeded(addEvents(backend));
        } else {
            backend.logMessage(actor.getNoun() + " can't go that way", MessageLog.MessageType.USER_ERROR);
            return ActionResult.Failed(null);
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
