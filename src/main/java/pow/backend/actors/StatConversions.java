package pow.backend.actors;

import pow.util.DieRoll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// enum to help convert stats into base values
// Assumes that all conversions are quadratic..
public enum StatConversions {
    CON_TO_HEALTH(0.25, 0, 0),
    INT_TO_MANA(0.25, 0, 0),
    DEX_TO_DEFENSE_AND_ATTACK(0.1, 0, 0),
    STR_TO_DAMAGE(0.05, 0, 0);  // player, main weapon; bow is 75% of this

    private double scale;
    private double shift;
    private int min;

    StatConversions(double scale, double shift, int min) {
        this.scale = scale;
        this.shift = shift;
        this.min = min;
    }

    public int getPoints(int stat) {
        return (int) Math.round(scale*(stat - min)*(stat - min) + shift);
    }
    public int getStat(int points) {
        if (points <= shift) return min;
        return (int) Math.round(Math.sqrt((points - shift)/scale) + min);
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
