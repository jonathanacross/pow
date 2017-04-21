package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class QuakeSpell implements Action {

    private final Actor actor;
    private final SpellParams spellParams;

    public QuakeSpell(Actor actor, SpellParams spellParams) {
        this.actor = actor;
        this.spellParams = spellParams;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();

        int damage = spellParams.getAmount(actor);
        String effectName = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.SMALL_BALL,
                SpellUtils.getEffectColor(spellParams.element),
                Direction.N); // dummy
        for (int radius = 1; radius <= spellParams.size; radius++) {
            List<Point> hitSquares = getHitSquares(gs, actor.loc, radius);
            events.add(GameEvent.Effect(new DungeonEffect(effectName, hitSquares)));
            for (Point s : hitSquares) {
                Actor m = gs.getCurrentMap().actorAt(s.x, s.y);
                if (m != null) {
                    events.addAll(AttackUtils.doHit(backend, actor, m, damage));
                }
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

    private List<Point> getHitSquares(GameState gs, Point center, int radius) {
        List<Point> candidates = new ArrayList<>();
        for (int i = 0; i < 2*radius; i++) {
            candidates.add(new Point(center.x - radius, center.y - radius + i));
            candidates.add(new Point(center.x - radius + i, center.y + radius));
            candidates.add(new Point(center.x + radius, center.y + radius - i));
            candidates.add(new Point(center.x + radius - i, center.y - radius));
        }

        GameMap map = gs.getCurrentMap();
        List<Point> squares = new ArrayList<>();
        for (Point c : candidates) {
            if (!map.isTerrainBlocked(actor, c.x, c.y)) {
                squares.add(c);
            }
        }

        return squares;
    }
}