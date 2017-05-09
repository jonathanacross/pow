package pow.backend.actors.ai;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.actors.Actor;
import pow.util.*;

import java.util.List;

public class SpellAi {

    // Returns whether it's appropriate for 'actor' to cast the spell given the current state.
    // TODO: remove 'target' as a parameter, make it a member of 'actor'.
    public static boolean canCastSpell(SpellParams spell, Actor actor, GameState gs, Actor target) {
        if (spell.requiredMana > actor.getMana()) return false;

        switch (spell.spellType) {
            case ARROW: return targetVisibleAndWithinRange(spell, actor, gs, target);
            case PHASE: return shouldCastPhase();
            case HEAL: return shouldCastHeal(actor);
            case BALL: return shouldCastBall(spell, actor, gs, target);
            case BOLT: return targetVisibleAndWithinRange(spell, actor, gs, target);
            case BREATH: return targetVisibleAndWithinRange(spell, actor, gs, target);
            case QUAKE: return shouldCastQuake(spell, actor, target);
            case CIRCLE_CUT: return targetVisibleAndWithinRange(spell, actor, gs, target);
            default: break;
        }
        return false;
    }

    private static boolean targetVisibleAndWithinRange(SpellParams spell, Actor actor, GameState gs, Actor target) {
        if (target == null) return false;
        if (!actorHasLineOfSight(actor, gs, target.loc)) return false;
        int dist2 = MathUtils.dist2(actor.loc, target.loc);
        return dist2 <= spell.size * spell.size;
    }

    private static boolean shouldCastBall(SpellParams spell, Actor actor, GameState gs, Actor target) {
        if (target == null) return false;
        if (!actorHasLineOfSight(actor, gs, target.loc)) return false;
        int dist2 = MathUtils.dist2(actor.loc, target.loc);
        // don't cast the spell if we're really close; we'll hurt ourselves!
        return dist2 > spell.size * spell.size;
    }

    private static boolean shouldCastQuake(SpellParams spell, Actor actor, Actor target) {
        if (target == null) return false;
        int dist = Math.abs(target.loc.x - actor.loc.x) + Math.abs(target.loc.y - actor.loc.y);
        return dist < spell.size;
    }

    private static boolean shouldCastPhase() {
        return true;
    }

    private static boolean shouldCastHeal(Actor actor) {
        return actor.getHealth() < actor.getMaxHealth();
    }

    private static boolean actorHasLineOfSight(Actor actor, GameState gs, Point target) {
        GameMap map = gs.getCurrentMap();
        int radius = Math.abs(target.x - actor.loc.x) + Math.abs(target.y - actor.loc.y);
        List<Point> ray = Bresenham.makeRay(actor.loc, target, radius + 1);
        ray.remove(0); // remove the actor from the path
        for (Point p : ray) {
            if (!map.isOnMap(p.x, p.y)) return false;
            if (map.map[p.x][p.y].blockAir()) return false;
            if (p.x == target.x && p.y == target.y) return true;
        }
        return false;
    }
}
