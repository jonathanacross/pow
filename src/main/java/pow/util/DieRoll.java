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

    // TODO: put in random class
    public int rollDice(Random rng) {
        int sum = 0;
        for (int i = 0; i < roll; i++) {
            sum += 1 + rng.nextInt(die);
        }
        return sum;
    }

    // parses a string of the form XdY
    public static DieRoll parseDieRoll(String s) {
        if (s == null || s.isEmpty() || s.equals("0")) {
            return new DieRoll(0, 0);
        }

        String[] parts = s.split("d", 2);
        int roll = Integer.parseInt(parts[0]);
        int die = Integer.parseInt(parts[1]);
        return new DieRoll(roll, die);
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
