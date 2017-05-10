package pow.backend.action;

import pow.backend.ActionParams;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.DungeonExit;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// class for the player to request movement -- may return a different movement
// if the player is confused
public class MoveRequest implements Action {
    private final int dx;
    private final int dy;
    private final Actor actor;
    private final boolean pause;

    public MoveRequest(Actor actor, int dx, int dy) {
        this(actor, dx, dy, false);
    }

    public MoveRequest(Actor actor, int dx, int dy, boolean pause) {
        this.actor = actor;
        this.dx = dx;
        this.dy = dy;
        this.pause = pause;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();

        if (actor.isConfused()) {
            int confuseDx = gs.rng.nextInt(3) - 1;
            int confuseDy = gs.rng.nextInt(3) - 1;
            return ActionResult.Failed(new Move(this.actor, confuseDx, confuseDy, pause));
        }
        else {
            return ActionResult.Failed(new Move(this.actor, dx, dy, pause));
        }
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
