package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.gen.Constants;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;


public class ExitPortal implements Action {

    private final Actor actor;
    private final String areaId;

    public ExitPortal(Actor actor, String areaId) {
        this.actor = actor;
        this.areaId = areaId;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        backend.getGameState().inPortal = false;
        List<GameEvent> events = new ArrayList<>();

        // If no area given, then exit back at current area.
        if (areaId == null || areaId.isEmpty()) {
            return ActionResult.Succeeded(events);
        }

        Point targetLoc = backend.getGameState().world.world.get(areaId).keyLocations.get(Constants.PORTAL_KEY_LOCATION_ID);
        //Point adjustedTargetLoc = new Point(targetLoc.x + dx, targetLoc.y + dy);
        backend.logMessage(actor.getNoun() + " feels time and space bending...", MessageLog.MessageType.USER_ERROR);
        return ActionResult.Failed(new GotoArea(areaId, targetLoc));
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public Actor getActor() {
        return null;
    }
}
