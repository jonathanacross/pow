package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class UnlockDoor implements Action {
    private final Actor actor;
    private final DungeonFeature newFeature;
    private final Point loc;
    private final int lockLevel;

    public UnlockDoor(Actor actor, Point loc, int lockLevel, DungeonFeature newFeature) {
        this.actor = actor;
        this.loc = loc;
        this.lockLevel = lockLevel;
        this.newFeature = newFeature;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        // make sure the player is capable of unlocking the door.
        if (lockLevel == 1 && !backend.getGameState().player.artifacts.hasKey()) {
            return ActionResult.Failed(null);
        }
        if (lockLevel == 2 && !backend.getGameState().player.artifacts.hasAllPearls()) {
            return ActionResult.Failed(null);
        }
        GameMap map = backend.getGameState().getCurrentMap();
        map.map[loc.x][loc.y].feature = newFeature;
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
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
