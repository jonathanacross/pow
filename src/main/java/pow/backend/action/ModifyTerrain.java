package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class ModifyTerrain implements Action {
    private final Actor actor;
    private final DungeonTerrain newTerrain;
    private final Point loc;

    public ModifyTerrain(Actor actor, Point loc, DungeonTerrain newTerrain) {
        this.actor = actor;
        this.loc = loc;
        this.newTerrain = newTerrain;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameMap map = backend.getGameState().getCurrentMap();
        map.map[loc.x][loc.y].terrain = newTerrain;
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
