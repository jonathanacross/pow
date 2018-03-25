package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonExit;
import pow.backend.dungeon.DungeonFeature;
import pow.util.Point;

public class TakeStairs implements Action {
    private final Actor actor;
    private final boolean up;

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
            backend.logMessage(actor.getPronoun() + " is not allowed up/down stairs", MessageLog.MessageType.DEBUG);
            return ActionResult.Failed(null);
        }

        DungeonFeature feature = gs.getCurrentMap().map[actor.loc.x][actor.loc.y].feature;
        if ((feature == null) ||
            (!feature.flags.stairsUp && this.up) ||
            (!feature.flags.stairsDown && !this.up)) {
            String dir = this.up ? "up" : "down";
            backend.logMessage("there are no " + dir + " stairs here", MessageLog.MessageType.USER_ERROR);
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
