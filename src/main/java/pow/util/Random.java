package pow.util;

import java.io.Serializable;

public class Random implements Serializable {
    private int randSeed;

    public Random(int randSeed) {
        this.randSeed = randSeed;
    }

    // used for updating random seeds
    public int next() {
        //TODO: pick new constants; these are too big for java; also have to check overflow
        randSeed = (randSeed * 1664525 + 1013904223) % 429496727;
        randSeed = Math.abs(randSeed);
        // constants from numerical recipes.
        //randSeed = (randSeed * 1664525 + 1013904223) % 4294967296;
        return randSeed;
    }

    // returns a number from 0 to [max]-1
    public int nextInt(int max) {
        next();
        // don't return the lowest bits -- they are NOT random.
        return (randSeed / 10) % max;
    }

    public double nextDouble() {
        next();
        return (randSeed / 2147483648.0);
    }
}
