package pow.backend.actors.ai;

import pow.backend.AttackData;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.action.Action;
import pow.backend.action.Attack;
import pow.backend.action.Move;
import pow.backend.actors.Actor;
import pow.backend.actors.Energy;
import pow.backend.actors.Player;
import pow.backend.utils.AttackUtils;
import pow.util.MathUtils;
import pow.util.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pow.util.MathUtils.dist2;

public class PetAi {

    public static Action getAction(Actor actor, GameState gs) {
        if (actor.isConfused()) {
            return actor.movement.wander(actor, gs);
        }

        // see if need to heal/group heal
        Action preferredAction = healIfNecessary(actor, gs);
        if (preferredAction != null) {
            return preferredAction;
        }

        // if very far away from the player, then try to catch up
        preferredAction = moveTowardOtherIfFar(actor, gs, 10);
        if (preferredAction != null) {
            return preferredAction;
        }

        // see if there's an actor we want to attack
        Actor targetEnemy = getPrimaryTarget(actor, gs);

        // next try to attack.
        if (targetEnemy != null) {
            return attackOrMoveToTarget(actor, gs, targetEnemy);
        }

        // if modestly far, and nothing else to do, get closer
        preferredAction = moveTowardOtherIfFar(actor, gs, 3);
        if (preferredAction != null) {
            return preferredAction;
        }

        // nothing important to do; just rest.
        return new Move(actor, 0, 0);
    }

    private static Action moveTowardOtherIfFar(Actor me, GameState gs, int maxDist) {
        Actor other = getOtherPartyActor(me, gs);
        int distSq = dist2(me.loc, other.loc);
        if (distSq >= maxDist * maxDist) {
            return me.movement.moveTowardTarget(me, gs, other.loc);
        }
        return null;
    }

    public static Player getOtherPartyActor(Actor me, GameState gs) {
        return (me == gs.party.player) ? gs.party.pet : gs.party.player;
    }

    private static int healthDeltaMinusWasted(Actor actor, SpellParams spell) {
        int idealDeltaHealth = spell.getSecondaryAmount(actor);
        int actualDeltaHealth =
                Math.min(actor.health + idealDeltaHealth, actor.getMaxHealth()) - actor.health;
        int wasted = idealDeltaHealth - actualDeltaHealth;

        return actualDeltaHealth - wasted;
    }

    private static double healScoreForSpell(Actor me, GameState gs, SpellParams spell) {
        // The heal score is computed as:
        // (change in health of party) - (mana to cast spell) - (amount of heath "wasted" by spell)
        //
        // The first term tries to maximize the health restored to the party,
        // so that if players are badly wounded then we will try to cast a big spell.
        // The second term tries to minimize the mana we waste, so if we can heal ourselves
        // fully with less mana then we'll prefer to do that.
        // The third term tries to avoid calling heal spells in an inefficient way;
        // that is, if a spell could heal 10% of health, and we only need to heal 5%, then
        // we're wasting our mana a bit.  This helps reduce healing when we're mostly full.
        //
        // An action of doing nothing corresponds to 0.  A positive score means the heal is
        // useful, and a negative score means the heal is not helpful.  (So casting
        // big expensive heal spells on nearly full health will have a large negative score.)

        if (!canCastSpell(me, spell)) {
            return 0.0;
        }
        if (spell.spellType == SpellParams.SpellType.HEAL) {
            return healthDeltaMinusWasted(me, spell) - spell.requiredMana;
        } else if (spell.spellType == SpellParams.SpellType.GROUP_HEAL) {
            Actor other = getOtherPartyActor(me, gs);
            return healthDeltaMinusWasted(me, spell) + healthDeltaMinusWasted(other, spell) - spell.requiredMana;
        } else {
            // not a heal spell.
            return 0.0;
        }
    }

    private static Action healIfNecessary(Actor me, GameState gs) {
        SpellParams bestSpell = null;
        // Note that the scoring function for healing spells is such that 0
        // is neutral, equivalent to doing nothing, so we must do strictly
        // better to this.
        double bestScore = 0;

        for (SpellParams spell : me.spells) {
            double score = healScoreForSpell(me, gs, spell);
            if (score > bestScore) {
                bestSpell = spell;
                bestScore = score;
            }
        }

        if (bestSpell != null) {
            return SpellParams.buildAction(bestSpell, me, null);
        } else {
            return null;
        }
    }

    private static double attackScoreForPrimaryAttack(Actor me, Actor target) {
        AttackData attack = me.getPrimaryAttack();
        double hitProb = AttackUtils.hitProb(attack.plusToHit, target.getDefense());
        return attackScore(me, target, attack, SpellParams.Element.NONE, hitProb, 0.0);
    }

    private static double attackScoreForSpell(Actor me, Actor target, SpellParams spell) {
        AttackData attack = new AttackData(spell.getPrimaryAmount(me), 0, 0);
        return attackScore(me, target, attack, spell.element, 1.0, spell.requiredMana);
    }

    // Score represents the expected amount of total health lost if we try to kill a monster
    // with this attack.  The best attack is the one that minimize the score.
    private static double attackScore(Actor me, Actor target, AttackData attack, SpellParams.Element element, double hitProb, double manaPerAttack) {
        double healthPerMana = getHealthPerMana(me);
        double avgMonsterDamage = MonsterDanger.getAverageDamagePerTurn(target, me);
        double avgMonsterTurnsPerPlayerTurn = Energy.getAverageTurnRatio(target.getSpeed(), me.getSpeed());

        double avgPlayerDamage = MonsterDanger.getAverageDamageForAttack(hitProb, attack, me, element);
        double expectedNumPlayerTurns = Math.ceil(target.health / avgPlayerDamage);
        double expectedNumMonsterTurns = (expectedNumPlayerTurns - 1) * avgMonsterTurnsPerPlayerTurn;

        double expectedTotalLifeLoss = expectedNumMonsterTurns * avgMonsterDamage;
        double expectedTotalManaLoss = expectedNumPlayerTurns * manaPerAttack;

        return expectedTotalLifeLoss + healthPerMana * expectedTotalManaLoss;
    }

    private static boolean canCastSpell(Actor actor, SpellParams spell) {
        return spell.minLevel <= actor.level && spell.requiredMana <= actor.mana;
    }

    // Gets rough equivalency of health and mana based on assumption of lesser heal
    // spell, which costs 3 mana and heals either 10% of health or 10 hp, whichever
    // is greater.
    private static double getHealthPerMana(Actor me) {
        double healthPerCast = Math.max(me.getMaxHealth() * 0.10, 10.0);
        double manaPerCast = 3;
        return healthPerCast / manaPerCast;
    }

    private static Action attackOrMoveToTarget(Actor me, GameState gs, Actor target) {
        // find best attack
        double bestScore = attackScoreForPrimaryAttack(me, target);
        SpellParams bestSpell = null;
        for (SpellParams spell : me.spells) {
            if (!canCastSpell(me, spell) || !SpellAi.canHitTarget(spell, me, gs, target)) {
                continue;
            }
            double score = attackScoreForSpell(me, target, spell);
            if (score < bestScore) {
                bestSpell = spell;
                bestScore = score;
            }
        }

        // see if we can do the attack
        if (bestSpell != null) {
            if (SpellAi.shouldMonsterCastSpell(bestSpell, me, gs, target)) {
                return SpellParams.buildAction(bestSpell, me, target.loc);
            }
        } else {
            // physical attack must have been best
            if (me.movement.canHit(me, target)) {
                return new Attack(me, target);
            }
        }

        // Couldn't do attack, but have a target.  Move towards it
        // so we can attack it next turn.
        return me.movement.moveTowardTarget(me, gs, target.loc);
    }

    // return if there's a dangerous actor that the other player is engaging.
    private static Actor primaryDangerousActor(Actor me, GameState gs) {
        Actor other = getOtherPartyActor(me, gs);
        if (other == null) {
            return null;
        }
        Point target = other.getTarget();
        if (target == null) {
            return null;
        }
        Actor monsterTarget = gs.getCurrentMap().actorAt(target.x, target.y);
        if (monsterTarget == null) {
            return null;
        }
        MonsterDanger.Danger danger = MonsterDanger.getDanger(other, monsterTarget);
        if (danger == MonsterDanger.Danger.SAFE ||
                danger == MonsterDanger.Danger.NORMAL ||
                danger == MonsterDanger.Danger.UNSAFE) {
            // monster must be sufficiently dangerous.
            return null;
        }
        if (!AiUtils.enemyIsWithinRange(me, monsterTarget, 11)) {
            return null;
        }
        return monsterTarget;
    }

    private static final Map<MonsterDanger.Danger, Double> dangerToHurtAmount;

    static {
        dangerToHurtAmount = new HashMap<>();
        dangerToHurtAmount.put(MonsterDanger.Danger.DEADLY, 0.2);
        dangerToHurtAmount.put(MonsterDanger.Danger.DANGEROUS, 0.4);
        dangerToHurtAmount.put(MonsterDanger.Danger.UNSAFE, 0.6);
        dangerToHurtAmount.put(MonsterDanger.Danger.NORMAL, 0.8);
        dangerToHurtAmount.put(MonsterDanger.Danger.SAFE, 1.0);
    }

    private static List<Actor> getTargets(Actor me, GameState gs) {
        List<Actor> targets = new ArrayList<>();
        for (Actor target : gs.getCurrentMap().actors) {
            if (target.friendly == me.friendly) {
                continue;
            }
            if (!me.canSeeLocation(gs, target.loc)) {
                continue;
            }
            if (!AiUtils.enemyIsWithinRange(me, target, 7)) {
                continue;
            }
            MonsterDanger.Danger danger = MonsterDanger.getDanger(me, target);
            // if dangerous, it better be hurt for us to attempt it.
            if (target.health > dangerToHurtAmount.get(danger) * target.getMaxHealth()) {
                continue;
            }
            targets.add(target);
        }
        return targets;
    }

    public static Actor getPrimaryTarget(Actor me, GameState gs) {
        Actor dangerousTarget = primaryDangerousActor(me, gs);
        if (dangerousTarget != null) {
            return dangerousTarget;
        }
        List<Actor> targets = getTargets(me, gs);
        Actor bestTarget = null;
        double bestScore = 10000000;

        for (Actor target : targets) {
            int score = MathUtils.dist2(me.loc, target.loc) +
                    (AiUtils.actorHasLineOfSight(me, gs, target.loc) ? 0 : 100);
            if (bestTarget == null || score < bestScore) {
                bestTarget = target;
                bestScore = score;
            }
        }
        return bestTarget;
    }
}
