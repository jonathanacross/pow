package utils;

import pow.backend.actors.StatConversions;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

// Utility for filling in fields of monsters.tsv automatically.
// Numbers in here are heuristic, and can be fiddled with to improve game balance.
public class MakeMonsterStats {

    private static double getBase(int area) {
        return 1.2 * area + 5;
    }

    private static int getSpeed(int area, int relativeSpeed) {
        int baseSpeed = 0;
        if (area >= 13) baseSpeed = 1;
        if (area >= 15) baseSpeed = 2;
        if (area >= 17) baseSpeed = 3;
        if (area >= 19) baseSpeed = 4;

        return baseSpeed + relativeSpeed;
    }

    private static int getConstitution(int area, String type, Set<String> flags) {
        double baseCon = getBase(area);

        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.7; break;
            case "insect": scaleFactor = 0.78; break;
            case "fish": scaleFactor = 0.8; break;
            case "bird": scaleFactor = 0.78; break;
            case "jelly": scaleFactor = 0.86; break;
            case "magical": scaleFactor = 0.76; break;
            case "mage": scaleFactor = 0.81; break;
            case "warrior": scaleFactor = 1.0; break;
            case "archer": scaleFactor = 0.9; break;
            case "rogue": scaleFactor = 0.9; break;
            case "reptile": scaleFactor = 1.05; break;
            case "giant": scaleFactor = 1.15; break;
            case "dragon": scaleFactor = 1.2; break;
            case "mammal": scaleFactor = 1.0; break;
            case "undead": scaleFactor = 0.85; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.3;
        if (flags.contains("finalboss")) scaleFactor *= 1.5;
        if (flags.contains("weak")) scaleFactor *= 0.8;

        double conApprox = scaleFactor * baseCon;

        return (int) Math.round(conApprox);
    }

    private static int getIntelligence(int area, String type, Set<String> flags) {
        double baseInt = getBase(area);

        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.3; break;
            case "insect": scaleFactor = 0.6; break;
            case "fish": scaleFactor = 0.6; break;
            case "bird": scaleFactor = 0.75; break;
            case "jelly": scaleFactor = 0.4; break;
            case "magical": scaleFactor = 1.2; break;
            case "mage": scaleFactor = 1.5; break;
            case "warrior": scaleFactor = 0.83; break;
            case "archer": scaleFactor = 0.9; break;
            case "rogue": scaleFactor = 0.9; break;
            case "reptile": scaleFactor = 0.7; break;
            case "giant": scaleFactor = 0.95; break;
            case "dragon": scaleFactor = 0.9; break;
            case "mammal": scaleFactor = 0.87; break;
            case "undead": scaleFactor = 1.05; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.1;
        if (flags.contains("finalboss")) scaleFactor *= 1.2;
        if (flags.contains("weak")) scaleFactor *= 0.8;

        int intApprox = (int) Math.round(baseInt * scaleFactor);

        return intApprox;
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
        double baseAttack = getBase(area);
        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.80; break;
            case "insect": scaleFactor = 0.80; break;
            case "fish": scaleFactor = 0.90; break;
            case "bird": scaleFactor = 0.85; break;
            case "jelly": scaleFactor = 0.8; break;
            case "magical": scaleFactor = 0.75; break;
            case "mage": scaleFactor = 0.85; break;
            case "warrior": scaleFactor = 1.08; break;
            case "archer": scaleFactor = 0.95; break;
            case "rogue": scaleFactor = 0.9; break;
            case "reptile": scaleFactor = 1.1; break;
            case "giant": scaleFactor = 1.2; break;
            case "dragon": scaleFactor = 1.1; break;
            case "mammal": scaleFactor = 1.05; break;
            case "undead": scaleFactor = 0.90; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.1;
        if (flags.contains("weak")) scaleFactor *= 0.9;
        if (flags.contains("strong")) scaleFactor *= 1.1;

        int strength = (int) Math.round(baseAttack * scaleFactor);

        return strength;
    }


    private static int getDexterity(int area, String type, Set<String> flags) {
        double baseDex = getBase(area);

        double scaleFactor;
        switch (type) {
            case "fungus": scaleFactor = 0.77; break;
            case "insect": scaleFactor = 0.9; break;
            case "fish": scaleFactor = 1.08; break;
            case "bird": scaleFactor = 0.95; break;
            case "jelly": scaleFactor = 0.63; break;
            case "magical": scaleFactor = 1.05; break;
            case "mage": scaleFactor = 0.83; break;
            case "warrior": scaleFactor = 1.05; break;
            case "archer": scaleFactor = 0.95; break;
            case "rogue": scaleFactor = 0.95; break;
            case "reptile": scaleFactor = 1.1; break;
            case "giant": scaleFactor = 1.05; break;
            case "dragon": scaleFactor = 1.0; break;
            case "mammal": scaleFactor = 0.9; break;
            case "undead": scaleFactor = 1.05; break;
            default: scaleFactor = 1.0;
        }

        // account for various flags/attributes
        if (flags.contains("boss")) scaleFactor *= 1.2;
        if (flags.contains("finalboss")) scaleFactor *= 1.4;
        if (flags.contains("weak")) scaleFactor *= 0.6;

        int dexterity = (int) Math.round(baseDex * scaleFactor);
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
//                        "damage" + "\t" +
//                        "defense" + "\t" +
//                        "hp" + "\t" +
//                        "mp" + "\t" +
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

//            int damage = StatConversions.STR_TO_DAMAGE.getPoints(strength);
//            int defense = StatConversions.DEX_TO_DEFENSE_AND_ATTACK.getPoints(dexterity);
//            int hp = StatConversions.CON_TO_HEALTH.getPoints(constitution);
//            int mp = StatConversions.INT_TO_MANA.getPoints(intelligence);

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
//                            damage + "\t" +
//                            defense + "\t" +
//                            hp + "\t" +
//                            mp + "\t" +
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
