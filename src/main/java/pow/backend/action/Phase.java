package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Arc;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
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
        List<GameEvent> events = new ArrayList<>();

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


        if (validSquares.isEmpty()) {
            backend.logMessage(actor.getPronoun() + " can't phase anywhere.");
        } else {
            Point targetLoc = validSquares.get(gs.rng.nextInt(validSquares.size()));

            String effectName = DungeonEffect.getEffectName(
                    DungeonEffect.EffectType.SMALL_BALL,
                    DungeonEffect.EffectColor.YELLOW,
                    Direction.N); // dummy
            List<Point> arcPoints = Arc.createArc(targetLoc, actor.loc);
            for (Point p : arcPoints) {
                events.add(GameEvent.Effect(new DungeonEffect(effectName, p)));
            }

            actor.loc = targetLoc;
            if (actor == gs.player) {
                gs.player.floorTarget = null;
                gs.player.monsterTarget = null;
                gs.getCurrentMap().updatePlayerVisibilityData(gs.player);
            }
            backend.logMessage(actor.getPronoun() + " phase.");

            events.add(GameEvent.DungeonUpdated());
        }
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
