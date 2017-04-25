package utils;

import pow.backend.actors.StatConversions;
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
        if (area >= 13) baseSpeed = 1;
        if (area >= 15) baseSpeed = 2;
        if (area >= 17) baseSpeed = 3;
        if (area >= 19) baseSpeed = 4;

        return baseSpeed + relativeSpeed;
    }

    private static int getConstitution(int area, String type, Set<String> flags) {
        //double baseHP = 6.0 * Math.pow(1.25, area);
        double baseHP = 1.3*area*area + 6.2;

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

        int desiredHP = (int) Math.round(baseHP * scaleFactor);
        int constitution = StatConversions.CON_TO_HEALTH.getStat(desiredHP);

        return constitution;
    }

    private static int getIntelligence(int area, String type, Set<String> flags) {
        //double baseMana = 3.0 * Math.pow(1.25, area);
        double baseMana = 0.65*area*area + 3.1;

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

        int desiredMana = (int) Math.round(baseMana * scaleFactor);
        int intelligence = StatConversions.INT_TO_MANA.getStat(desiredMana);

        return intelligence;
    }

    private static int getExperience(int area, String type, Set<String> flags, int constitution, int speed) {
        int hp = StatConversions.CON_TO_HEALTH.getPoints(constitution);
        double experience = hp/2.0;

        double scaleFactor = 1.0;
        if (flags.contains("erratic")) scaleFactor *= 0.7;
        if (flags.contains("stationary")) scaleFactor *= 0.5;
        // TODO: add spells in calculation

        double speedExpFactor = Math.pow(1.2, speed);

        return (int) Math.round(experience * scaleFactor * speedExpFactor);
    }

    private static int getStrength(int area, String type, Set<String> flags) {
        double baseAttack = 0.08*area*area + 3;
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

        int damage = (int) Math.round(baseAttack * scaleFactor);
        int strength = StatConversions.STR_TO_DAMAGE.getPoints(damage);

        return strength;
    }


    private static int getDexterity(int area, String type, Set<String> flags) {
        double baseDef = 0.10*area*area + area + 5;

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

        int toHit = (int) Math.round(baseDef * scaleFactor);
        int dexterity = StatConversions.DEX_TO_DEFENSE_AND_ATTACK.getStat(toHit);

        return dexterity;
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
                "str" + "\t" +
                "dex" + "\t" +
                "int" + "\t" +
                "con" + "\t" +
                "speed" + "\t" +
                "experience" + "\t" +
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
            int strength = getStrength(area, type, flags);
            int dexterity = getDexterity(area, type, flags);
            int intelligence = getIntelligence(area, type, flags);
            int constitution = getConstitution(area, type, flags);
            int experience = getExperience(area, type, flags, constitution, speed);
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
                    strength + "\t" +
                    dexterity + "\t" +
                    intelligence + "\t" +
                    constitution + "\t" +
                    speed + "\t" +
                    experience + "\t" +
                    gameFlagsStr + "\t" +
                    spellFlagsStr + "\t" +
                    uniqueItemDrops + "\t" +
                    numDropChances);

        }
        writer.close();

        System.out.println("Done updating monsters.tsv");
    }
}
