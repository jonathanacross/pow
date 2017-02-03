package pow.backend.action;

import pow.backend.ActionParams;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonExit;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class TakeStairs implements Action {
    Actor actor;
    boolean up;

    public TakeStairs(Actor actor, boolean up) {
        this.actor = actor;
        this.up = up;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();

        if (actor != gs.player) {
            backend.logMessage(actor.getPronoun() + " is not allowed up/down stairs");
            return ActionResult.Failed(null);
        }

        DungeonFeature feature = gs.getCurrentMap().map[actor.loc.x][actor.loc.y].feature;
        if ((feature == null) ||
            (!feature.flags.stairsUp && this.up) ||
            (!feature.flags.stairsDown && !this.up)) {
            String dir = this.up ? "up" : "down";
            backend.logMessage("there are no " + dir + " stairs here");
            return ActionResult.Failed(null);
        }

        DungeonExit exit = new DungeonExit(feature.actionParams.name);
        String targetArea = exit.areaId;
        Point targetLoc = gs.world.world.get(exit.areaId).keyLocations.get(exit.locName);
        return ActionResult.Failed(new GotoArea(targetArea, targetLoc));
    }

    @Override
    public boolean consumesEnergy() { return false; }
}
