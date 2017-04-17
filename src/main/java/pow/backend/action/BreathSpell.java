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

public class BreathSpell implements Action {

    private final Actor actor;
    private final Point target;
    private final SpellParams spellParams;

    public BreathSpell(Actor actor, Point target, SpellParams spellParams) {
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
            List<Point> effectSquares = SpellUtils.getBreathArea(gs, actor.loc, target, radius);
            events.add(GameEvent.Effect(new DungeonEffect(effectName, effectSquares)));
        }

        // hit everything in the large area once
        List<Point> hitSquares = SpellUtils.getBreathArea(gs, actor.loc, target, spellParams.size);
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
