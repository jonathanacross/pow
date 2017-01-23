package pow.util;

import java.io.Serializable;
import java.util.Random;

public class DieRoll implements Serializable {
    public int roll;
    public int die;
    public int plus;

    public DieRoll(int roll, int die, int plus) {
        this.roll = roll;
        this.die = die;
        this.plus = plus;
    }

    // TODO: put in random class
    public int rollDice(Random rng) {
        int sum = plus;
        for (int i = 0; i < roll; i += 1) {
            sum += 1 + rng.nextInt(die);
        }
        return sum;
    }

    // parses a string of the forms:
    // XdY+Z
    // XdY
    // Z
    public static DieRoll parseDieRoll(String s) {
        if (s == null) {
            return new DieRoll(0, 0, 0);
        }

        boolean hasDie = s.contains("d");
        if (!hasDie) {
            return new DieRoll(0, 0, Integer.parseInt(s));
        }

        String[] parts1 = s.split("d");
        int roll = Integer.parseInt(parts1[0]);
        boolean hasPlus = s.contains("+");
        int die;
        int plus;
        if (hasPlus) {
            String[] parts2 = parts1[1].split("\\+");
            die = Integer.parseInt(parts2[0]);
            plus = Integer.parseInt(parts2[1]);
        } else {
            die = Integer.parseInt(parts1[1]);
            plus = 0;
        }
        return new DieRoll(roll, die, plus);
    }

    @Override
    public String toString() {
        if (roll == 0) {
            return Integer.toString(plus);
        }

        if (plus == 0) {
            return roll + "d" + die;
        }

        return roll + "d" + die + " + " + plus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DieRoll dieRoll = (DieRoll) o;

        if (roll != dieRoll.roll) return false;
        if (die != dieRoll.die) return false;
        return plus == dieRoll.plus;
    }

    @Override
    public int hashCode() {
        int result = roll;
        result = 1001 * result + die;
        result = 1001 * result + plus;
        return result;
    }
}
