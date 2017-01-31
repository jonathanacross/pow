package utils;

public class MakePlayerExpLevels {
    public static void main(String[] args) {
        // probably want this higher than the factor in baseHP for monster health, in makeMonsterStats
        // but not so much that it's a grind..
        int firstLevelNeeded = 10;
        double increaseRate = 1.65;
        int numLevels = 21;

        int[] amountToReachNextLevel = new int[numLevels];
        int[] total = new int[numLevels];
        for (int i = 0; i < numLevels; i++) {
            amountToReachNextLevel[i] = (int) Math.round(firstLevelNeeded * Math.pow(increaseRate, i-1));
            if (i > 0) {
                total[i] = total[i-1] + amountToReachNextLevel[i];
            }
        }

        for (int i = 0; i < numLevels; i++) {
            System.out.println(i + "\t" + amountToReachNextLevel[i] + "\t" + total[i]);
        }
    }
}
