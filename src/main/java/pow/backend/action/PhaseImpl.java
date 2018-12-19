package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.util.Point;

public class PhaseImpl implements Action {

    private final Actor actor;
    private final Point targetLoc;

    public PhaseImpl(Actor actor, Point targetLoc) {
        this.actor = actor;
        this.targetLoc = targetLoc;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        actor.loc = targetLoc;
        GameState gs = backend.getGameState();
        if (actor == gs.party.player) {
            gs.party.player.target.clear();
            gs.getCurrentMap().updatePlayerVisibilityData(gs.party.player, gs.party.pet);
        } else if (actor == gs.party.pet) {
            gs.party.pet.target.clear();
            gs.getCurrentMap().updatePlayerVisibilityData(gs.party.player, gs.party.pet);
        }
        backend.logMessage(actor.getNoun() + " phases.", MessageLog.MessageType.GENERAL);
        return ActionResult.succeeded(GameEvent.DUNGEON_UPDATED);
    }

    @Override
    public boolean consumesEnergy() {
        return true;
    }

    @Override
    public Actor getActor() {
        return actor;
    }
}
