package pow.backend.action;

import pow.backend.*;
import pow.backend.utils.AttackUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Spell that casts various types of arrows.  There's a fair amount of code
// in common with the Arrow Action, but the parameters for hitting, doing damage,
// and losing physical arrows are different enough that it's not worth combining
// them.
public class ArrowSpell implements Action {
    private final Actor attacker;
    private final Point target;
    private final SpellParams spellParams;

    public ArrowSpell(Actor attacker, Point target, SpellParams spellParams) {
        this.attacker = attacker;
        this.target = target;
        this.spellParams = spellParams;
    }

    @Override
    public Actor getActor() {
        return this.attacker;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<Action> subactions = new ArrayList<>();
        GameMap map = gs.getCurrentMap();

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, spellParams.size + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.ARROW,
                DungeonEffect.EffectColor.NONE,
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        AttackUtils.HitParams hitParams = new AttackUtils.HitParams(spellParams, attacker, backend.getGameState().rng);
        for (Point p : ray) {
            subactions.add(new ShowEffect(new DungeonEffect(effectId, p)));
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null) {
                if (defender.friendly != attacker.friendly) {
                    subactions.add(new Hit(attacker, defender, hitParams));
                }
                break;
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
        }

        // clear out last effect.
        subactions.add(new ShowEffect(new DungeonEffect(Collections.emptyList())));
        return ActionResult.failed(subactions);
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }
}
