package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.dungeon.gen.TerrainData;
import pow.backend.event.GameEvent;
import pow.util.Array2D;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class Move implements Action {
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
        events.add(GameEvent.Moved());
        GameState gs = backend.getGameState();

        // temporary custom code to demonstrate winning/losing
        if (actor == gs.player) {
            DungeonFeature feature = gs.world.currentMap.map[actor.loc.x][actor.loc.y].feature;
            if (feature != null && feature.id.equals("wintile")) {
                backend.logMessage("you won!");
                events.add(GameEvent.WonGame());
            } else if (feature != null && feature.id.equals("losetile")) {
                backend.logMessage("you died.");
                events.add(GameEvent.LostGame());
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


        int newx = actor.loc.x + dx;
        int newy = actor.loc.y + dy;


        // if the current square is a teleport tile at the edge of the screen,
        // and the player tries to move off, then go the the appropriate area.
        DungeonSquare[][] currMap = gs.world.currentMap.map;
        int width = Array2D.width(currMap);
        int height = Array2D.height(currMap);
        DungeonTerrain currSquareTerrain = gs.world.currentMap.map[actor.loc.x][actor.loc.y].terrain;
        if (currSquareTerrain.flags.teleport) {
            if (newx < 0 || newx >= width || newy < 0 || newy >= height) {
                String encodedLoc = currSquareTerrain.actionParams.name;
                String[] fields = encodedLoc.split("@");
                String targetArea = fields[0];
                Point targetLoc = gs.world.world.get(targetArea).keyLocations.get(fields[1]);
                return ActionResult.Failed(new GotoArea(this.actor, targetArea, targetLoc));
            }
        }

        Actor defender = gs.world.currentMap.actorAt(newx, newy);
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

        DungeonTerrain terrain = gs.world.currentMap.map[newx][newy].terrain;
        if (terrain.flags.actOnStep) {
            Point loc = new Point(newx, newy);
            ActionParams params = terrain.actionParams.clone();
            params.point = loc;
            Action newAction = ActionParams.buildAction(this.actor, params);
            return ActionResult.Failed(newAction);
        }

        DungeonFeature feature = gs.world.currentMap.map[newx][newy].feature;
        if (feature != null && feature.flags.actOnStep) {
            Point loc = new Point(newx, newy);
            ActionParams params = feature.actionParams.clone();
            params.point = loc;
            Action newAction = ActionParams.buildAction(this.actor, params);
            return ActionResult.Failed(newAction);
        }

        if (! gs.world.currentMap.isBlocked(newx, newy)) {
            // move
            actor.loc.x = newx;
            actor.loc.y = newy;
            if (actor == gs.player) {
                gs.world.currentMap.updatePlayerVisibilityData(gs.player);
            }
            return ActionResult.Succeeded(addEvents(backend));
        } else {
            backend.logMessage(actor.getPronoun() + " can't go that way");
            return ActionResult.Failed(null);
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
