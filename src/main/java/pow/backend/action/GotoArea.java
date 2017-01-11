package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.GameWorld;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class GotoArea implements Action {
    private Actor actor;
    private String areaName;
    private Point loc;

    public GotoArea(Actor actor, String areaName, Point loc) {
        this.actor = actor;
        this.areaName = areaName;
        this.loc = loc;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameWorld world = backend.getGameState().world;
        // TODO: add a default area that always exists, and put the player there
        // if the world doesn't contain areaName.
        world.currentMap = world.world.get(areaName);
        actor.loc.x = loc.x;
        actor.loc.y = loc.y;
        actor.energy.setFull(); // make sure the player can move first
        // TODO: move the pet, too!
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }

    @Override
    public Actor getActor() {
        return this.actor;
    }
}
