package pow.backend.event;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.util.Point;

import java.util.Collections;
import java.util.List;

public class Phase implements GameEvent {

    private final Actor actor;
    private final Point targetLoc;

    public Phase(Actor actor, Point targetLoc) {
        this.actor = actor;
        this.targetLoc = targetLoc;
    }

    @Override
    public List<GameEvent> process(GameBackend backend) {
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
        return Collections.emptyList();
    }

    @Override
    public EventType getEventType() {
        return EventType.DUNGEON_UPDATED;
    }

    @Override
    public boolean showUpdate() {
        return false;
    }
}
