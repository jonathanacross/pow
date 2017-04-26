package pow.backend.actors;

import pow.util.DieRoll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatComputations {

    public static int constitutionToHealth(int stat) {
        return (int) Math.round(0.25 * stat * stat);
    }

    public static int intelligenceToMana(int stat) {
        return (int) Math.round(0.25 * stat * stat);
    }

    public static int dexterityToDefenseAndAttack(int stat) {
        return (int) Math.round(0.1 * stat * stat);
    }

    public static int strengthToDamage(int stat) {
        return (int) Math.round(0.05 * stat * stat);
    }

    public static DieRoll findClosestDieRoll(double value) {
        // build possible die rolls
        List<DieRoll> dieRolls = new ArrayList<>();
        for (int roll = 1; roll <= 15; roll++) {
            for (int die = 1; die <= 40; die++) {
                dieRolls.add(new DieRoll(roll, die));
            }
        }
        Collections.reverse(dieRolls);

        double bestDist = Double.MAX_VALUE;
        DieRoll bestDieRoll = null;
        for (DieRoll dieRoll : dieRolls) {
            double avgDamage = dieRoll.roll * (dieRoll.die + 1.0) / 2.0;
            // Distance computed by two things.
            // 1. Want the avg damage to match the target value approximately.
            // 2. Want some variability, but not too much:
            //    1d3 (avg 2) is better than 2d1,
            //    1d5 (avg 3) is worse than 2d2.
            //    so, we try to get the die size to be 2x as big as the number of rolls.
            double dist = Math.abs(avgDamage - value)*10 + Math.pow(2*dieRoll.roll - dieRoll.die,2);
            if (dist <= bestDist) {
                bestDist = dist;
                bestDieRoll = dieRoll;
            }
        }
        return bestDieRoll;
    }
}
