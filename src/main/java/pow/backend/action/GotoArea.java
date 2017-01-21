package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.GameWorld;
import pow.backend.actors.Actor;
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

        // remove player and pet from current area
        gs.world.currentMap.removeActor(gs.player);
        if (gs.pet != null) {
            gs.world.currentMap.removeActor(gs.pet);
        }

        // set the new area
        world.currentMap = world.world.get(areaName);

        // set up player/pet in the new area
        world.currentMap.placePlayerAndPet(gs.player, loc, gs.pet);

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
