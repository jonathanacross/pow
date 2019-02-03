package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.utils.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Phase implements Action {

    private final Actor actor;
    private final int phaseMax;

    public Phase(Actor actor, int phaseMax) {
        this.actor = actor;
        this.phaseMax = phaseMax;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<Action> subactions = new ArrayList<>();

        // Find squares we can phase to.
        // Legal locations appear in a square annulus centered on the player
        List<Point> validSquares = new ArrayList<>();
        int phaseMin = phaseMax / 2;

        for (int dx = -phaseMax; dx <= phaseMax; dx++) {
            for (int dy = -phaseMax; dy <= phaseMax; dy++) {
                // Skip squares in the center of the annulus.
                if (Math.abs(dx) <= phaseMin && Math.abs(dy) <= phaseMin) continue;

                int x = actor.loc.x + dx;
                int y = actor.loc.y + dy;

                // Make sure it's on the map, and that we can go there.
                if (gs.getCurrentMap().isBlocked(actor, x, y)) continue;

                validSquares.add(new Point(x,y));
            }
        }

        // Require that squares are also contiguous to the current player,
        // to disallow phasing through locked doors, etc.
        List<Point> contiguousSquares = gs.getCurrentMap().findAccessibleSquares(actor, actor.loc, false);
        validSquares.retainAll(contiguousSquares);

        if (validSquares.isEmpty()) {
            backend.logMessage(actor.getNoun() + " can't phase anywhere.",
                    MessageLog.MessageType.USER_ERROR);
        } else {
            Point targetLoc = validSquares.get(gs.rng.nextInt(validSquares.size()));

            String effectName = DungeonEffect.getEffectName(
                    DungeonEffect.EffectType.SMALL_BALL,
                    DungeonEffect.EffectColor.YELLOW,
                    Direction.N); // dummy
            List<Point> arcPoints = SpellUtils.createArc(actor.loc, targetLoc);
            for (Point p : arcPoints) {
                subactions.add(new ShowEffect(new DungeonEffect(effectName, p)));
            }

            PhaseImpl phase = new PhaseImpl(actor, actor, targetLoc);
            subactions.add(phase);

            // clear out last effect.
            subactions.add(new ShowEffect(new DungeonEffect(Collections.emptyList())));
        }
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
