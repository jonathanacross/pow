package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.GameWorld;
import pow.backend.actors.Actor;
import pow.backend.dungeon.gen.GeneratorUtils;
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

        // clear any targets
        gs.player.floorTarget = null;
        gs.player.monsterTarget = null;

        // remove player and pet from current area
        gs.getCurrentMap().removeActor(gs.player);
        if (gs.pet != null) {
            gs.getCurrentMap().removeActor(gs.pet);
        }

        // set the new area, add monsters if needed
        boolean needsRegen = gs.world.recentMaps.setMap(gs.world.world.get(areaName));
        if (needsRegen) {
            GeneratorUtils.regenMonstersForCurrentMap(gs.getCurrentMap(), gs.rng);
        }

        // set up player/pet in the new area
        gs.getCurrentMap().placePlayerAndPet(gs.player, loc, gs.pet);

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
