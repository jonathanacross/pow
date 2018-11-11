package pow.backend.actors.ai;

import pow.backend.AttackData;
import pow.backend.SpellParams;
import pow.backend.action.AttackUtils;
import pow.backend.actors.Actor;

// This class allows evaluation for how dangerous a particular monster is.
public class MonsterDanger {
    public enum Danger {
        SAFE,  // green
        NORMAL,       // yellow
        UNSAFE,     // orange
        DANGEROUS,  // red
        DEADLY  // purple
    }

    public static Danger getDanger(Actor defender, Actor attacker) {
        double likelyDamage = getRoughMaxDamage(attacker, defender);

        double remainingHealth = defender.health;
        if (likelyDamage >= remainingHealth) {
            return Danger.DEADLY;
        }
        if (likelyDamage >= remainingHealth / 2) {
            return Danger.DANGEROUS;
        }
        if (likelyDamage < remainingHealth / 10) {
            return Danger.SAFE;
        }
        if (likelyDamage < remainingHealth / 4) {
            return Danger.NORMAL;
        }
        return Danger.UNSAFE;
    }

    // Considers each possible type of attack (physical and spells) and get the estimated
    // average damage.  This isn't entirely realistic, as it doesn't take into account
    // detailed monster behavior; it assumes every type of attack is equally likely, while
    // (depending on AI implementation) may prefer melee attacks, or cast spells
    // that don't cause damage.
    public static double getAverageDamagePerTurn(Actor attacker, Actor defender) {
        int numAttacks = 0;
        AttackData physicalAttack = attacker.getPrimaryAttack();
        double physicalHitProb = AttackUtils.hitProb(physicalAttack.plusToHit, defender.getDefense());
        double totalDamage = getAverageDamageForAttack(physicalHitProb, physicalAttack, defender, SpellParams.Element.NONE);
        numAttacks++;

        for (SpellParams spellParams : attacker.spells) {
            if (!SpellParams.isAttackSpell(spellParams)) {
                continue;
            }
            AttackData spellAttack = new AttackData(spellParams.getPrimaryAmount(attacker), 0, 0);
            totalDamage += getAverageDamageForAttack(1.0, spellAttack, defender, spellParams.element);
            numAttacks++;
        }

        return totalDamage / numAttacks;
    }

    // How much we expect to get hit, overestimated a bit to be on safe side.
    public static double getAverageDamageForAttack(double hitProb, AttackData attack, Actor defender, SpellParams.Element element) {
        // Expected raw damage.
        int baseDamage = (int) Math.ceil(hitProb * attack.getAverageDamage());

        // Take into account resistances.
        double adjustedDamage = AttackUtils.adjustDamage(baseDamage, element, defender);
        return adjustedDamage;
    }

    // Considers each possible type of attack (physical and all spells) and get the estimated
    // maximum damage of the most deadly attack.
    private static double getRoughMaxDamage(Actor attacker, Actor defender) {
        AttackData physicalAttack = attacker.getPrimaryAttack();
        double physicalHitProb = AttackUtils.hitProb(physicalAttack.plusToHit, defender.getDefense());
        double maxDamage = getRoughMaxDamageForAttack(physicalHitProb, physicalAttack, defender, SpellParams.Element.NONE);

        for (SpellParams spellParams : attacker.spells) {
            if (!SpellParams.isAttackSpell(spellParams)) {
                continue;
            }
            AttackData spellAttack = new AttackData(spellParams.getPrimaryAmount(attacker), 0, 0);
            double likelySpellDamage = getRoughMaxDamageForAttack(1.0, spellAttack, defender, spellParams.element);
            maxDamage = Math.max(maxDamage, likelySpellDamage);
        }

        // Multiply by the max number of turns the attacker could take before the defender can move.
        return maxDamage * getMaxConsecutiveTurns(defender, attacker);
    }

    // How much we expect to get hit, overestimated a bit to be on safe side.
    private static double getRoughMaxDamageForAttack(double hitProb, AttackData attack, Actor defender, SpellParams.Element element) {
        // Chance of getting hit.  Overestimate by 2x to be safe.
        double scale = Math.min(2.0 * hitProb, 1.0);

        // Expected damage assuming a hit: use 95% confidence interval rather than mean
        // (or the max, which are both misleading).
        int baseDamage = (int) Math.ceil(scale * upper95(attack));

        // Take into account resistances.
        double adjustedDamage = AttackUtils.adjustDamage(baseDamage, element, defender);
        return adjustedDamage;
    }

    // Damage from upper 95% confidence interval.
    private static double upper95(AttackData attackData) {
        return attackData.getAverageDamage() + 1.96 * Math.sqrt(attackData.getVariance());
    }

    // How many turns (max) can attacker have before defender has one?
    private static int getMaxConsecutiveTurns(Actor defender, Actor attacker) {
        // Every +3, the speed will double; see Energy.java.
        // So, e.g., actors with +1, +2, +3 vs another will have possibly
        // 2 consecutive turns, actors with +4, +5, +6 may get 3 consecutive turns, etc.
        int diff = attacker.getSpeed() - defender.getSpeed();
        return Math.min((diff + 5) / 3, 1);
    }
}
