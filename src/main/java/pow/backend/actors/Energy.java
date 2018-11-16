package pow.backend.actors;

import pow.util.MathUtils;
import java.io.Serializable;

// Energy is used to control the rate that actors move relative to other
// actors. Each game turn, every actor will accumulate energy based on their
// speed. When it reaches a threshold, that actor can take a turn.
public class Energy implements Serializable {
    // TODO: consider whether it's worth using my original speed notation,
    // where 1 = normal speed, 2 = double speed, 0.5 = half, etc.
    // speeds must be computed relative to 1.0, but give a little more
    // flexibility and understandability.  Revisit this around the time
    // when I start applying speed bonuses/penalties.
    private static final int MIN_SPEED = 0;
    private static final int NORMAL_SPEED = 6;
    private static final int MAX_SPEED = 12;

    private static final int ACTION_COST = 240;

    // How much energy is gained each game turn for each speed.
    // Each increase is approximately 2^(1/3) = 1.25992105 times larger
    // than the previous,  which means for every +3 to the index, the
    // speed will double.
    private static final int[] gains = {
            15,     // 1/4 normal speed
            19,
            24,
            30,     // 1/2 normal speed
            38,
            48,
            60,     // normal speed
            76,
            95,
            120,    // 2x normal speed
            151,
            190,
            240     // 4x normal speed
            };

    private int energy = 0;

    public void setFull() {
        energy = ACTION_COST;
    }

    public boolean canTakeTurn() {
        return energy >= ACTION_COST;
    }

    private static int getEnergy(int speed) {
        int index = MathUtils.clamp(speed + NORMAL_SPEED, MIN_SPEED, MAX_SPEED);
        return gains[index];
    }

    // Advances one game turn and gains an appropriate amount of energy. Returns
    // `true` if there is enough energy to take a turn.
    public boolean gain(int speed) {
        energy += getEnergy(speed);
        return canTakeTurn();
    }

    // Spends a turn's worth of energy.
    public void spend() {
        if (energy < ACTION_COST) {
            throw new RuntimeException("tried to spend energy to take an action, but didn't have enough");
        }
        energy -= ACTION_COST;
    }

    // If actor1 has speed1 and actor2 has speed2, this returns how many turns
    // actor1 will have (on average) for each turn actor2 has.
    public static double getAverageTurnRatio(int speed1, int speed2) {
        return 1.0 * getEnergy(speed1) / getEnergy(speed2);
    }
}
