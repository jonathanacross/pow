package utils;

import pow.backend.actors.Monster;
import pow.backend.dungeon.gen.MonsterGenerator;
import pow.util.Point;

import java.util.*;

public class MakePlayerExpLevels {

    // Thoughts on how to compute experience levels:
    // 1. take avg monster experience for each level.
    // 2. compute approx desired number of monsters to kill each level.
    //    current thought is something like ln(level + 0.1)*75.
    // 3. multiply, then curve fit.

    // Current estimate is: let y = exp to next level, and x = level.  Then
    // ln(y) = 3.78*x^0.367

    // If it's too easy to go up levels using current monsters, then
    // consider Angband's approach: compute monster exp by multiplying
    // by the factor of monster level / char level.

    public static double getAvgExperience(List<Monster> monsters) {
        double expTotal = 0.0;
        int count = 0;
        for (Monster m: monsters) {
            expTotal += m.experience;
            count++;
        }
        return expTotal / count;
    }

    public static void showAverageMonsterExpByLevel() {
        Random rng = new Random(123);
        Set<String> allMonsters = MonsterGenerator.getMonsterIds();

        Map<Integer, List<Monster>> monstersByLevel = new HashMap<>();
        for (String id: allMonsters) {
            Monster m = MonsterGenerator.genMonster(id, rng, new Point(-1,-1));
            int level = m.level;

            if (!monstersByLevel.containsKey(level)) {
                monstersByLevel.put(level, new ArrayList<>());
            }
            monstersByLevel.get(level).add(m);
        }

        List<Integer> levels = new ArrayList<>();
        levels.addAll(monstersByLevel.keySet());
        Collections.sort(levels);
        for (int level : levels) {
            System.out.println(level + "\t" + getAvgExperience(monstersByLevel.get(level)));
        }
    }

    public static void makeCharacterExperienceTable() {
        // probably want this higher than the factor in baseHP for monster health, in makeMonsterStats
        // but not so much that it's a grind..
//        int firstLevelNeeded = 10;
//        double increaseRate = 1.45;
        int numLevels = 21;

        int[] amountToReachNextLevel = new int[numLevels];
        int[] total = new int[numLevels];
        for (int i = 0; i < numLevels; i++) {
            amountToReachNextLevel[i] = (int) Math.round(Math.exp(3.78*Math.pow(i,0.367)));
            if (i > 0) {
                total[i] = total[i-1] + amountToReachNextLevel[i];
            }
        }

        for (int i = 0; i < numLevels; i++) {
            System.out.println(i + "\t" + amountToReachNextLevel[i] + "\t" + total[i]);
        }
    }

    public static void main(String[] args) {
        //showAverageMonsterExpByLevel();
        makeCharacterExperienceTable();
    }
}
