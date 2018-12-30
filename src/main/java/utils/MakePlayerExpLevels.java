package utils;

import pow.backend.actors.Monster;
import pow.backend.dungeon.gen.MonsterGenerator;
import pow.util.Point;

import java.util.*;

public class MakePlayerExpLevels {

    // To compute experience levels:
    // 1. take avg monster experience (non-bosses) for each level, and curve fit.
    // 2. compute approx desired number of monsters to kill each level.
    //    current thought is something like 50 - 40/level.
    // 3. multiply.

    // If it's too easy to go up levels using current monsters, then
    // consider Angband's approach: compute monster exp by multiplying
    // by the factor of monster level / char level.

    private static double getAvgExperience(List<Monster> monsters) {
        double expTotal = 0.0;
        int count = 0;
        for (Monster m: monsters) {
            expTotal += m.experience;
            count++;
        }
        return expTotal / count;
    }

    private static CurveFit.Params computeAverageMonsterExpByLevel() {
        Random rng = new Random(123);
        Set<String> allMonsters = MonsterGenerator.getMonsterIds();

        Map<Integer, List<Monster>> monstersByLevel = new HashMap<>();
        for (String id: allMonsters) {
            Monster m = MonsterGenerator.genMonster(id, rng, new Point(-1,-1));
            int level = m.level;
            // hacky way to skip bosses
            if (m.requiredItemDrops.size() > 0) continue;

            if (!monstersByLevel.containsKey(level)) {
                monstersByLevel.put(level, new ArrayList<>());
            }
            monstersByLevel.get(level).add(m);
        }

        List<Integer> levels = new ArrayList<>(monstersByLevel.keySet());
        Collections.sort(levels);
        for (int level : levels) {
//            for (Monster m: monstersByLevel.get(level)) {
//                System.out.println(level + "\t" + m.name + "\t" + m.experience);
//            }
            System.out.println(level + "\t" + getAvgExperience(monstersByLevel.get(level)));
        }

        double[] x = new double[levels.size()];
        double[] y = new double[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            x[i] = levels.get(i);
            y[i] = getAvgExperience(monstersByLevel.get(i));
        }
        CurveFit.Params monsterExpParams = CurveFit.expFit(x, y);
        System.out.println("curve fit: avgExp = " + monsterExpParams.a + " * exp(" + monsterExpParams.b + " * x)");
        return monsterExpParams;
    }

    private static void makeCharacterExperienceTable() {
        CurveFit.Params monsterExpParams = computeAverageMonsterExpByLevel();

        int numLevels = 21;
        int[] amountToReachNextLevel = new int[numLevels];
        int[] total = new int[numLevels];
        for (int i = 0; i < numLevels; i++) {
            double desiredMonstersToKill = 50.0 - 40.0/i;
            //double desiredMonstersToKill = 50.0 * Math.log(i + 0.2);
            double expPerMonster = monsterExpParams.a * Math.exp(monsterExpParams.b * i);
            amountToReachNextLevel[i] = (int) Math.round(desiredMonstersToKill * expPerMonster);
            if (i > 0) {
                total[i] = total[i-1] + amountToReachNextLevel[i];
            }
        }

        for (int i = 0; i < numLevels; i++) {
            System.out.println(i + "\t" + amountToReachNextLevel[i] + "\t" + total[i]);
        }
    }

    public static void main(String[] args) {
        makeCharacterExperienceTable();
    }
}
