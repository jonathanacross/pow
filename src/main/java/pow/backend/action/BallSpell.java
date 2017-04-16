package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.action.spell.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class BallSpell implements Action {

    private final Actor actor;
    private final Point target;
    private final SpellParams spellParams;

    public BallSpell(Actor actor, Point target, SpellParams spellParams) {
        this.actor = actor;
        this.target = target;
        this.spellParams = spellParams;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();

        // draw effects
        String effectName = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.LARGE_BALL,
                SpellUtils.getEffectColor(spellParams.element),
                Direction.N); // dummy
        for (int radius = 1; radius <= spellParams.size; radius++) {
            List<Point> effectSquares = SpellUtils.getBallArea(gs, target, radius);
            events.add(GameEvent.Effect(new DungeonEffect(effectName, effectSquares)));
        }
        for (int radius = spellParams.size - 1; radius >= 1; radius--) {
            List<Point> effectSquares = SpellUtils.getBallArea(gs, target, radius);
            events.add(GameEvent.Effect(new DungeonEffect(effectName, effectSquares)));
        }

        // hit everything in the large ball once
        List<Point> hitSquares = SpellUtils.getBallArea(gs, target, spellParams.size);
        int damage = spellParams.getAmount(actor);

        for (Point s : hitSquares) {
            Actor m = gs.getCurrentMap().actorAt(s.x, s.y);
            if (m != null) {
                AttackUtils.doHit(backend, actor, m, damage);
            }
        }

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
