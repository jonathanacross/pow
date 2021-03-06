package pow.backend.action;
import pow.backend.*;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonExit;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
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

    private List<GameEvent> addEvents() {
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.MOVED);
        if (pause) {
            events.add(GameEvent.EFFECT);
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
        GameMap map = gs.getCurrentMap();

        // just stay still
        if (dx == 0 && dy == 0) {
            return ActionResult.succeeded(addEvents());
        }

        int newx = actor.loc.x + dx;
        int newy = actor.loc.y + dy;

        // Check that the target location is valid.  The ONLY case where it is
        // valid to move off the map is if the actor == player, and the 
        // player is on a tile that lets them go to another area.
        if (!map.isOnMap(newx, newy)) {
            // check for player changing maps
            if (gs.party.containsActor(actor)) {
                DungeonTerrain currSquareTerrain = map.map[actor.loc.x][actor.loc.y].terrain;
                if (currSquareTerrain.flags.teleport) {
                    DungeonExit exit = new DungeonExit(currSquareTerrain.actionParams.name);
                    String targetArea = exit.areaId;
                    Point targetLoc = gs.world.world.get(exit.areaId).keyLocations.get(exit.locName);
                    return ActionResult.failed(new GotoArea(targetArea, targetLoc));
                }
            }

            backend.logMessage(actor.getNoun() + " can't go that way", MessageLog.MessageType.DEBUG);
            return ActionResult.failed();
        }

        // Check if we should attack
        Actor defender = map.actorAt(newx, newy);
        if (defender != null) {
            if (!defender.friendly)  {
                // attack
                return ActionResult.failed(new Attack(this.actor, defender));
            }
            else {
                // friendly, swap positions
                return ActionResult.failed(new Swap(this.actor, defender));
            }
        }

        DungeonTerrain terrain = map.map[newx][newy].terrain;
        if (terrain.flags.actOnStep) {
            if (!terrain.flags.diggable || actor.canDig()) {
                Point loc = new Point(newx, newy);
                ActionParams params = new ActionParams(terrain.actionParams);
                params.point = loc;
                Action newAction = ActionParams.buildAction(this.actor, params);
                return ActionResult.failed(newAction);
            }
        }

        DungeonFeature feature = map.map[newx][newy].feature;
        if (feature != null && feature.flags.actOnStep) {
            if (feature.flags.stairs) {
                // special case if the feature is stairs, as we have to compute final destination
                // location based on the direction of the player.
                DungeonExit exit = new DungeonExit(feature.actionParams.name);
                String targetArea = exit.areaId;
                Point targetLoc = gs.world.world.get(exit.areaId).keyLocations.get(exit.locName);
                Point adjustedTargetLoc = new Point(targetLoc.x + dx, targetLoc.y + dy);
                return ActionResult.failed(new GotoArea(targetArea, adjustedTargetLoc));
            } else {
                Point loc = new Point(newx, newy);
                ActionParams params = new ActionParams(feature.actionParams);
                params.point = loc;
                Action newAction = ActionParams.buildAction(this.actor, params);
                return ActionResult.failed(newAction);
            }
        }

        if (! map.isBlocked(this.actor, newx, newy)) {
            Point previousLoc = new Point(actor.loc.x, actor.loc.y);

            // move
            actor.loc.x = newx;
            actor.loc.y = newy;
            if (actor == gs.party.selectedActor) {
                if (gs.party.pet != null && map.petCouldNotBePlaced) {
                    // If the pet couldn't previously be placed, it can now,
                    // using the space where the player was.
                    backend.logMessage(gs.party.pet.getNoun() + " rejoins you", MessageLog.MessageType.GENERAL);
                    map.addActor(gs.party.pet);
                    gs.party.pet.loc = previousLoc;
                    map.petCouldNotBePlaced = false;
                }
                map.updatePlayerVisibilityData(gs.party.player, gs.party.pet);
            }
            for (Player p : gs.party.playersInParty()) {
                // update the party's targeting, in case the player or target move
                // out of visibility range.
                p.target.update(gs, p);
            }
            return ActionResult.succeeded(addEvents());
        } else {
            backend.logMessage(actor.getNoun() + " can't go that way", MessageLog.MessageType.USER_ERROR);
            return ActionResult.failed();
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
