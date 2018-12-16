package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenPortal implements Action {
    private final Actor actor;
    private final Point loc;
    private final DungeonFeature newFeature;

    public OpenPortal(Actor actor, Point loc, DungeonFeature newFeature) {
        this.actor = actor;
        this.loc = loc;
        this.newFeature = newFeature;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        // Require the player to have the portal key.
        if (!backend.getGameState().party.artifacts.hasPortalKey()) {
            backend.logMessage(actor.getNoun() + " doesn't have the right key to unlock this door", MessageLog.MessageType.USER_ERROR);
            return ActionResult.Failed(null);
        }

        // Unlock the portal in the Gameworld.
        Map<String, MapPoint.PortalStatus> portals = backend.getGameState().world.topologySummary.getPortals();
        String areaName = backend.getGameState().getCurrentMap().id;
        portals.put(areaName, MapPoint.PortalStatus.OPEN);

        // Update the feature.
        GameMap map = backend.getGameState().getCurrentMap();
        map.map[loc.x][loc.y].feature = newFeature;

        backend.logMessage(actor.getNoun() + " unlocks the portal door", MessageLog.MessageType.GENERAL);
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEventOld.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() {
        return true;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }
}
