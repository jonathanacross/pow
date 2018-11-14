package pow.backend.actors.ai;

import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.actors.Actor;
import pow.util.*;

public class SpellAi {

    // Returns whether the actor can hit the target with the spell
    public static boolean canHitTarget(SpellParams spell, Actor actor, GameState gs, Actor target) {
        if (target == null) {
            return false;
        }

        switch (spell.spellType) {
            case ARROW: return targetVisibleAndWithinRange(spell, actor, gs, target);
            case BALL: return AiUtils.actorHasLineOfSight(actor, gs, target.loc);
            case BOLT: return targetVisibleAndWithinRange(spell, actor, gs, target);
            case BREATH: return targetVisibleAndWithinRange(spell, actor, gs, target);
            case QUAKE: return quakeWithinRange(spell, actor, target);
            case CIRCLE_CUT: return targetVisibleAndWithinRange(spell, actor, gs, target);
            case CHAIN: return targetVisibleAndWithinRange(spell, actor, gs, target);

            // other spells don't attack
            default: return false;
        }
    }

    // Returns whether it's appropriate for 'actor' to cast the spell given the current state.
    public static boolean shouldMonsterCastSpell(SpellParams spell, Actor actor, GameState gs, Actor target) {
        if (spell.requiredMana > actor.getMana()) return false;

        switch (spell.spellType) {
            case ARROW: return canHitTarget(spell, actor, gs, target);
            case HEAL: return canHeal(actor);
            case PHASE: return true;
            case BALL: return canHitTarget(spell, actor, gs, target);
            case BOLT: return canHitTarget(spell, actor, gs, target);
            case BREATH: return canHitTarget(spell, actor, gs, target);
            case QUAKE: return canHitTarget(spell, actor, gs, target);
            case CIRCLE_CUT: return canHitTarget(spell, actor, gs, target);
            default: break;
        }
        return false;
    }

    private static boolean targetVisibleAndWithinRange(SpellParams spell, Actor actor, GameState gs, Actor target) {
        if (!AiUtils.actorHasLineOfSight(actor, gs, target.loc)) return false;
        int dist2 = MathUtils.dist2(actor.loc, target.loc);
        return dist2 <= spell.size * spell.size;
    }

    private static boolean quakeWithinRange(SpellParams spell, Actor actor, Actor target) {
        int dist = Math.abs(target.loc.x - actor.loc.x) + Math.abs(target.loc.y - actor.loc.y);
        return dist < spell.size;
    }

    private static boolean canHeal(Actor actor) {
        return actor.getHealth() < actor.getMaxHealth();
    }
}
