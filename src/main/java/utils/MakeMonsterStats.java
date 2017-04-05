package utils;

import pow.util.DieRoll;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

// Utility for filling in fields of monsters.tsv automatically.
// Numbers in here are heuristic, and can be fiddled with to improve game balance.
public class MakeMonsterStats {

    private static int getSpeed(int area, int relativeSpeed) {
        int baseSpeed = 0;
        if (area == 6) baseSpeed = 1;
        if (area == 7) baseSpeed = 2;
        if (area == 8) baseSpeed = 3;
        if (area == 9) baseSpeed = 4;

        return baseSpeed + relativeSpeed;
    }

    private static int getHP(int area, String type, Set<String> flags) {
        double baseHP = 6.0 * Math.pow(1.25, area);

        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.55; break;
            case "insect": scaleFactor = 0.60; break;
            case "jelly": scaleFactor = 0.75; break;
            case "mage": scaleFactor = 0.6667; break;
            case "warrior": scaleFactor = 1.0; break;
            case "archer": scaleFactor = 0.8; break;
            case "rogue": scaleFactor = 0.8; break;
            case "reptile": scaleFactor = 1.1; break;
            case "giant": scaleFactor = 1.3333; break;
            case "dragon": scaleFactor = 1.5; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 2;
        if (flags.contains("finalboss")) scaleFactor *= 3;
        if (flags.contains("weak")) scaleFactor *= 0.5;

        return (int) Math.round(baseHP * scaleFactor);
    }

    private static int getMana(int area, String type, Set<String> flags) {
        double baseMana = 3.0 * Math.pow(1.25, area);

        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.1; break;
            case "insect": scaleFactor = 0.4; break;
            case "jelly": scaleFactor = 0.2; break;
            case "mage": scaleFactor = 1.5; break;
            case "warrior": scaleFactor = 0.7; break;
            case "archer": scaleFactor = 0.8; break;
            case "rogue": scaleFactor = 0.8; break;
            case "reptile": scaleFactor = 0.5; break;
            case "giant": scaleFactor = 0.9; break;
            case "dragon": scaleFactor = 1.2; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.5;
        if (flags.contains("finalboss")) scaleFactor *= 1.5;
        if (flags.contains("weak")) scaleFactor *= 0.5;

        return (int) Math.round(baseMana * scaleFactor);
    }

    private static int getExperience(int area, String type, Set<String> flags, int hp, int speed) {
        double experience = hp/2.0;

        double scaleFactor = 1.0;
        if (flags.contains("erratic")) scaleFactor *= 0.7;
        if (flags.contains("stationary")) scaleFactor *= 0.5;

        double speedExpFactor = Math.pow(1.2, speed);

        return (int) Math.round(experience * scaleFactor * speedExpFactor);
    }

    private static double getAttackTarget(int area, String type, Set<String> flags) {
        //double baseAttack = 1.0 * Math.pow(1.63, area);
        double baseAttack = 0.125*area*area + 3;
        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.60; break;
            case "insect": scaleFactor = 0.75; break;
            case "jelly": scaleFactor = 0.65; break;
            case "mage": scaleFactor = 0.7; break;
            case "warrior": scaleFactor = 1.0; break;
            case "archer": scaleFactor = 0.9; break;
            case "rogue": scaleFactor = 0.8; break;
            case "reptile": scaleFactor = 1.2; break;
            case "giant": scaleFactor = 1.5; break;
            case "dragon": scaleFactor = 1.2; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.2;
        if (flags.contains("weak")) scaleFactor *= 0.8;
        if (flags.contains("strong")) scaleFactor *= 1.25;

        return baseAttack * scaleFactor;
    }

    private static DieRoll findClosestDieRoll(double value) {
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

    private static int getDefense(int area, String type, Set<String> flags) {
        double baseDef = 0.12*area*area + area + 5;

        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.6; break;
            case "insect": scaleFactor = 0.8; break;
            case "jelly": scaleFactor = 0.4; break;
            case "mage": scaleFactor = 0.7; break;
            case "warrior": scaleFactor = 1.0; break;
            case "archer": scaleFactor = 0.9; break;
            case "rogue": scaleFactor = 0.9; break;
            case "reptile": scaleFactor = 1.4; break;
            case "giant": scaleFactor = 1.2; break;
            case "dragon": scaleFactor = 1.3; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.2;
        if (flags.contains("finalboss")) scaleFactor *= 1.4;
        if (flags.contains("weak")) scaleFactor *= 0.6;

        return (int) Math.round(baseDef * scaleFactor);
    }

    private static int getToHit(int area, String type, Set<String> flags) {
        double baseToHit = 0.12*area*area + area + 5;

        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.75; break;
            case "insect": scaleFactor = 0.90; break;
            case "jelly": scaleFactor = 0.85; break;
            case "mage": scaleFactor = 0.7; break;
            case "warrior": scaleFactor = 1.0; break;
            case "archer": scaleFactor = 0.9; break;
            case "rogue": scaleFactor = 0.9; break;
            case "reptile": scaleFactor = 1.1; break;
            case "giant": scaleFactor = 1.1; break;
            case "dragon": scaleFactor = 1.2; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.5;
        if (flags.contains("finalboss")) scaleFactor *= 1.75;
        if (flags.contains("weak")) scaleFactor *= 0.8;

        return (int) Math.round(baseToHit * scaleFactor);
    }

    private static Set<String> getFlags(String field) {
        String[] fields = field.split(",");
        Set<String> flags = new HashSet<>();
        for (String tok : fields) {
            if (!tok.isEmpty()) {
                flags.add(tok);
            }
        }
        return flags;
    }

    public static void main(String[] args) throws IOException {
        InputStream is = MakeMonsterStats.class.getResourceAsStream("/data/monsters-input.tsv");
        TsvReader reader = new TsvReader(is);

        List<String[]> data = reader.getData();

        PrintWriter writer = new PrintWriter("src/main/resources/data/monsters.tsv", "UTF-8");

        writer.println(
                "#area" + "\t" +
                "id" + "\t" +
                "name" + "\t" +
                "image" + "\t" +
                "description" + "\t" +
                "hp" + "\t" +
                "mana" + "\t" +
                "attacks" + "\t" +
                "toHit" + "\t" +
                "defense" + "\t" +
                "experience" + "\t" +
                "speed" + "\t" +
                "flags" + "\t" +
                "spells" + "\t" +
                "artifactDrops" + "\t" +
                "numDropChances");

        for (String[] entry : data) {
            int area = Integer.parseInt(entry[0]);
            String id = entry[1];
            String type = entry[2];
            String genFlagsStr = entry[3];  // ideally, these will be removed, eventually made into good game flags
            String gameFlagsStr = entry[4];
            String spellFlagsStr = entry[5];
            int relativeSpeed = Integer.parseInt(entry[6]);
            String uniqueItemDrops = entry[7];
            int numDropChances = Integer.parseInt(entry[8]);
            String name = entry[9];
            String image = entry[10];
            String description = entry[11];

            Set<String> genFlags = getFlags(genFlagsStr);
            Set<String> gameFlags = getFlags(gameFlagsStr);
            Set<String> spellFlags = getFlags(spellFlagsStr);
            Set<String> flags = new HashSet<>();
            flags.addAll(genFlags);
            flags.addAll(gameFlags);
            flags.addAll(spellFlags);

            int speed = getSpeed(area, relativeSpeed);
            int hp = getHP(area, type, flags);
            int mana = getMana(area, type, flags);
            double attackAvg = getAttackTarget(area, type, flags);
            DieRoll attackDieRoll = findClosestDieRoll(attackAvg);
            int toHit = getToHit(area, type, flags);
            int defense = getDefense(area, type, flags);
            int experience = getExperience(area, type, flags, hp, speed);
//            System.out.println(hp + "\t" + attackDieRoll + "\t" +
//                            attackDieRoll.roll * (1.0 + attackDieRoll.die) / 2.0
//                    + "\t" + attackAvg + "\t" + id);
//            System.out.println(area + "\t" + attackAvg + "\t" + defense + "\t" + toHit + "\t" + speed + "\t" + experience + "\t" + id);

            writer.println(
                    area + "\t" +
                    id + "\t" +
                    name + "\t" +
                    image + "\t" +
                    description + "\t" +
                    hp + "\t" +
                    mana + "\t" +
                    attackDieRoll + "\t" +
                    toHit + "\t" +
                    defense + "\t" +
                    experience + "\t" +
                    speed + "\t" +
                    gameFlagsStr + "\t" +
                    spellFlagsStr + "\t" +
                    uniqueItemDrops + "\t" +
                    numDropChances);

        }
        writer.close();

        System.out.println("Done updating monsters.tsv");
    }
}
