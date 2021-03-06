package pow.backend.action;

import pow.backend.*;
import pow.backend.utils.AttackUtils;
import pow.backend.utils.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
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
        List<Action> subactions = new ArrayList<>();

        AttackUtils.HitParams hitParams = new AttackUtils.HitParams(spellParams, actor, backend.getGameState().rng);
        String effectName = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.SMALL_BALL,
                SpellUtils.getEffectColor(spellParams.element),
                Direction.N); // dummy
        for (int radius = 1; radius <= spellParams.size; radius++) {
            List<Point> hitSquares = getHitSquares(gs, actor.loc, radius);
            hitSquares.removeIf( (Point p) -> {
                Actor target = gs.getCurrentMap().actorAt(p.x, p.y);
                return (target != null) && (target.friendly == actor.friendly);
            } );
            subactions.add(new ShowEffect(new DungeonEffect(effectName, hitSquares)));
            for (Point s : hitSquares) {
                Actor m = gs.getCurrentMap().actorAt(s.x, s.y);
                if (m != null) {
                    subactions.add(new Hit(actor, m, hitParams));
                }
            }
        }

        // clear out last effect.
        subactions.add(new ShowEffect(new DungeonEffect(Collections.emptyList())));
        return ActionResult.failed(subactions);
    }

    @Override
    public boolean consumesEnergy() {
        return false;
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
