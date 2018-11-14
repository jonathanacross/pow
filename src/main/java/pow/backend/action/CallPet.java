package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.Party;
import pow.backend.utils.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class CallPet implements Action {

    private final Actor actor;

    public CallPet(Actor actor) {
        this.actor = actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();

        Party party = gs.party;
        if (party.pet == null) {
            backend.logMessage(party.player.getNoun() + " doesn't have a pet.",
                    MessageLog.MessageType.USER_ERROR);
            return ActionResult.Succeeded(events);
        }

        // Find squares we can phase to.
        Point targetLoc = gs.getCurrentMap().findClosestOpenSquare(party.pet, party.player.loc);
        if (targetLoc == null) {
            backend.logMessage("there is no place nearby.",
                    MessageLog.MessageType.USER_ERROR);
            return ActionResult.Succeeded(events);
        }

        String effectName = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.SMALL_BALL,
                DungeonEffect.EffectColor.YELLOW,
                Direction.N); // dummy
        List<Point> arcPoints = SpellUtils.createArc(party.pet.loc, targetLoc);
        for (Point p : arcPoints) {
            events.add(GameEvent.Effect(new DungeonEffect(effectName, p)));
        }

        party.pet.loc = targetLoc;
        party.pet.floorTarget = null;
        party.pet.monsterTarget = null;
        gs.getCurrentMap().updatePlayerVisibilityData(gs.party.player, gs.party.pet);
        backend.logMessage(party.pet.getNoun() + " phases.", MessageLog.MessageType.GENERAL);

        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
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
