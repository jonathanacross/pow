package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.Party;
import pow.backend.event.GameEvent;
import pow.backend.utils.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallPet implements Action {

    private final Actor actor;

    public CallPet(Actor actor) {
        this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<Action> subactions = new ArrayList<>();
        List<GameEvent> events = new ArrayList<>();

        Party party = gs.party;
        if (party.pet == null) {
            backend.logMessage(party.player.getNoun() + " doesn't have a pet.",
                    MessageLog.MessageType.USER_ERROR);
            return ActionResult.succeeded(events);
        }

        // Find squares we can phase to.
        Point targetLoc = gs.getCurrentMap().findClosestOpenSquare(party.pet, party.player.loc);
        if (targetLoc == null) {
            backend.logMessage("there is no place nearby for " + party.pet.getNoun() + " to phase to.",
                    MessageLog.MessageType.USER_ERROR);
            return ActionResult.succeeded(events);
        }

        String effectName = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.SMALL_BALL,
                DungeonEffect.EffectColor.YELLOW,
                Direction.N); // dummy
        List<Point> arcPoints = SpellUtils.createArc(party.pet.loc, targetLoc);
        for (Point p : arcPoints) {
            subactions.add(new ShowEffect(new DungeonEffect(effectName, p)));
        }

        subactions.add(new PhaseImpl(party.pet, targetLoc));

        // clear out last effect.
        // TODO: should this be new dungeonupdated?
        subactions.add(new ShowEffect(new DungeonEffect(Collections.emptyList())));
        return ActionResult.failed(subactions);
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
