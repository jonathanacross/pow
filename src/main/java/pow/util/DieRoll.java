package pow.util;

import java.io.Serializable;
import java.util.Random;

public class DieRoll implements Serializable {
    public final int roll;
    public final int die;

    public DieRoll(int roll, int die) {
        this.roll = roll;
        this.die = die;
    }

    public int rollDice(Random rng) {
        int sum = 0;
        for (int i = 0; i < roll; i++) {
            sum += 1 + rng.nextInt(die);
        }
        return sum;
    }

    @Override
    public String toString() {
        return roll + "d" + die;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DieRoll dieRoll = (DieRoll) o;

        if (roll != dieRoll.roll) return false;
        return die == dieRoll.die;
    }

    @Override
    public int hashCode() {
        int result = roll;
        result = 1001 * result + die;
        return result;
    }
}
