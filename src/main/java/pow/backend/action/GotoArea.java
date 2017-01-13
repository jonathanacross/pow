package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap; 
import pow.backend.GameState;
import pow.backend.GameWorld;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class GotoArea implements Action {
    private String areaName;
    private Point loc;

    public GotoArea(String areaName, Point loc) {
        this.areaName = areaName;
        this.loc = loc;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        GameWorld world = gs.world;
        // TODO: add a default area that always exists, and put the player there
        // if the world doesn't contain areaName.
        world.currentMap = world.world.get(areaName);
        gs.player.loc.x = loc.x;
        gs.player.loc.y = loc.y;
        gs.player.energy.setFull(); // make sure the player can move first
        // TODO: move the pet, too!
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }

    @Override
    public Actor getActor() {
        return null;
    }
}
