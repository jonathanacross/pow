package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class ModifyFeature implements Action {
    private final Actor actor;
    private final DungeonFeature newFeature;
    private final Point loc;

    public ModifyFeature(Actor actor, Point loc, DungeonFeature newFeature) {
        this.actor = actor;
        this.loc = loc;
        this.newFeature = newFeature;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameMap map = backend.getGameState().getCurrentMap();
        map.map[loc.x][loc.y].feature = newFeature;
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEventOld.DungeonUpdated());
        return ActionResult.succeeded(events);
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
