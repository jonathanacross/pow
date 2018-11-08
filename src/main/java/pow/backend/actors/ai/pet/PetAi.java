package pow.backend.actors.ai.pet;

import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.action.Action;
import pow.backend.action.Attack;
import pow.backend.actors.Actor;
import pow.util.MathUtils;

import java.util.ArrayList;
import java.util.List;

import static pow.util.MathUtils.dist2;

public class PetAi {

    public static Action getAction(Actor actor, GameState gs) {
        if (actor.isConfused()) {
            return actor.movement.wander(actor, gs);
        }

        // see if need to heal
        Action preferredAction = groupHealIfNecessary(gs);
        if (preferredAction != null) {
            return preferredAction;
        }
        preferredAction = healIfNecessary(gs);
        if (preferredAction != null) {
            return preferredAction;
        }

        // next try to attack.
        Actor closestEnemy = actor.movement.findNearestEnemy(actor, gs);
        if (closestEnemy != null && MathUtils.dist2(actor.loc, closestEnemy.loc) <= 2) {
            return new Attack(actor, closestEnemy);
        }

        // if "far away" from the human controlled player, then try to catch up
        int playerDist = dist2(actor.loc, gs.party.selectedActor.loc);
        if (playerDist >= 9) {
            return actor.movement.moveTowardTarget(actor, gs, gs.party.selectedActor.loc);
        }

        // move randomly
        return actor.movement.wander(actor, gs);
    }

    private static List<SpellParams> findSpells(Actor actor, SpellParams.SpellType spellType) {
        List<SpellParams> matchingSpells = new ArrayList<>();
        for (SpellParams params : actor.spells) {
            if (params.spellType == spellType) {
                matchingSpells.add(params);
            }
        }
        return matchingSpells;
    }

    private static Action groupHealIfNecessary(GameState gs) {
        // see if we need it
        Actor me = gs.party.selectedActor;
        Actor other = gs.party.isPetSelected() ? gs.party.player : gs.party.pet;
        if (other == null) {
            // only one party member.  Not worth using group heal.
            return null;
        }
        boolean bothHurt = (me.health <= 0.4 * me.getMaxHealth()) &&
                (other.health <= 0.4 * other.getMaxHealth());
        if (! bothHurt) {
            // both have to be sufficiently damaged
            return null;
        }

        List<SpellParams> groupHealSpells = findSpells(me, SpellParams.SpellType.GROUP_HEAL);
        if (groupHealSpells.isEmpty()) {
            // have to have the spell
            return null;
        }

        SpellParams groupHealSpell = groupHealSpells.get(0);
        if (me.mana < groupHealSpell.requiredMana) {
            // need to have enough mana
            return null;
        }

        return SpellParams.buildAction(groupHealSpell, me, null);
    }

    private static Action healIfNecessary(GameState gs) {
        Actor me = gs.party.selectedActor;

        List<SpellParams> healSpells = findSpells(me, SpellParams.SpellType.HEAL);
        if (healSpells.isEmpty()) {
            // don't have a heal spell
            return null;
        }

        boolean hurt = me.health <= 0.5 * me.getMaxHealth();
        boolean badlyHurt = me.health <= 0.3 * me.getMaxHealth();
        if (badlyHurt) {
            // get the strongest spell we can use
            SpellParams bestHeal = null;
            for (SpellParams heal : healSpells) {
                if ((me.mana >= heal.requiredMana) &&
                        (me.level >= heal.minLevel) &&
                        (bestHeal == null ||
                                (heal.getSecondaryAmount(me) > bestHeal.getSecondaryAmount(me)))) {
                    bestHeal = heal;
                }
            }
            return SpellParams.buildAction(bestHeal, me, null);
        } else if (hurt) {
            // get the least-mana spell to use
            SpellParams bestHeal = null;
            for (SpellParams heal : healSpells) {
                if ((me.mana >= heal.requiredMana) &&
                        (me.level >= heal.minLevel) &&
                        (bestHeal == null || (heal.requiredMana < bestHeal.requiredMana))) {
                    bestHeal = heal;
                }
            }
            return SpellParams.buildAction(bestHeal, me, null);
        }

        return null;
    }
}
