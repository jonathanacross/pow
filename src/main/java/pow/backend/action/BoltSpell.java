package pow.backend.action;

import pow.backend.*;
import pow.backend.utils.AttackUtils;
import pow.backend.utils.SpellUtils;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoltSpell implements Action {

    private final Actor attacker;
    private final Point target;
    private final SpellParams spellParams;

    public BoltSpell(Actor attacker, Point target, SpellParams spellParams) {
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

        backend.logMessage(attacker.getNoun() + " casts a" +
                AttackUtils.getDamageTypeString(spellParams.element) + " bolt", MessageLog.MessageType.COMBAT_NEUTRAL);

        List<Point> ray = Bresenham.makeRay(attacker.loc, target, spellParams.size + 1);
        String effectId = DungeonEffect.getEffectName(
                DungeonEffect.EffectType.BOLT,
                SpellUtils.getEffectColor(spellParams.element),
                Direction.getDir(attacker.loc, target));

        ray.remove(0); // remove the attacker from the path of the arrow.
        AttackUtils.HitParams hitParams = new AttackUtils.HitParams(spellParams, attacker, backend.getGameState().rng);
        for (Point p : ray) {
            Actor defender = map.actorAt(p.x, p.y);
            if (defender != null && defender.friendly != attacker.friendly) {
                subactions.add(new Hit(attacker, defender, hitParams));
            }
            if (!map.isOnMap(p.x, p.y)) break; // can happen if we fire through an exit
            if (map.map[p.x][p.y].blockAir()) break;
            subactions.add(new ShowEffect(new DungeonEffect(effectId, p)));
        }

        // clear out last effect.
        // TODO: should this be new dungeonupdated?
        subactions.add(new ShowEffect(new DungeonEffect(Collections.emptyList())));
        subactions.add(new CompletedAction(attacker));
        return ActionResult.failed(subactions);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
