package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.Party;
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
            backend.logMessage(party.player.name + " doesn't have a pet.",
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
        List<Point> arcPoints = createArc(party.pet.loc, targetLoc);
        for (Point p : arcPoints) {
            events.add(GameEvent.Effect(new DungeonEffect(effectName, p)));
        }

        party.pet.loc = targetLoc;
        party.pet.floorTarget = null;
        party.pet.monsterTarget = null;
        gs.getCurrentMap().updatePlayerVisibilityData(gs.party.player, gs.party.pet);
        backend.logMessage(party.pet.getPronoun() + " phases.", MessageLog.MessageType.GENERAL);

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

    // TODO: factor out as utility method; shared between phase and callpet
    private static List<Point> createArc(Point start, Point end) {
        final int height = 5;
        final int maxSteps = 20;

        List<Point> points = new ArrayList<>();

        double dx = end.x - start.x;
        double dy = end.y - start.y;
        int numSteps = (int) Math.min(2 * Math.max(Math.abs(dx), Math.abs(dy)), maxSteps);

        for (int i = 0; i <= numSteps; i++) {
            double t = (double) i / numSteps;
            double x = (1.0 - t) * start.x + t * end.x;
            double y = (1.0 - t) * start.y + t * end.y;
            double z = 4.0 * height * t * (1.0 - t);

            points.add(new Point((int) Math.round(x), (int) Math.round(y - z)));
        }
        return points;
    }
}
