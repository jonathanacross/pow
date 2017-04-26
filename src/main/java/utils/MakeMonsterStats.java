package utils;

import pow.backend.actors.StatComputations;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

// Utility for filling in fields of monsters.tsv automatically.
// Numbers in here are heuristic, and can be fiddled with to improve game balance.
public class MakeMonsterStats {

    private static final int STR_INDEX = 0;
    private static final int DEX_INDEX = 1;
    private static final int INT_INDEX = 2;
    private static final int CON_INDEX = 3;
    private static final Map<String, double[]> modifiersMap;
    static {
        String[] modifiersStrings = {
                //           str    dex    int    con
                "archer      0.95   0.95   0.9    0.9",
                "bird        0.85   0.95   0.75   0.78",
                "boss        1.1    1.2    1.1    1.3",
                "dragon      1.1    1.0    0.9    1.2",
                "finalboss   1.3    1.4    1.2    1.5",
                "fish        0.90   1.08   0.6    0.8",
                "fungus      0.80   0.77   0.3    0.7",
                "giant       1.2    1.05   0.95   1.15",
                "insect      0.80   0.9    0.6    0.78",
                "jelly       0.8    0.63   0.4    0.86",
                "mage        0.85   0.83   1.5    0.81",
                "magical     0.75   1.05   1.2    0.76",
                "mammal      1.05   0.9    0.87   1.0",
                "reptile     1.1    1.1    0.7    1.05",
                "rogue       0.9    0.95   0.9    0.9",
                "strong      1.1    1.0    1.0    1.1",
                "undead      0.90   1.05   1.05   0.85",
                "warrior     1.08   1.05   0.83   1.0",
                "weak        0.9    0.6    0.8    0.8"
        };
        modifiersMap = new HashMap<>();
        for (String s: modifiersStrings) {
            String[] fields = s.split("\\s+");
            modifiersMap.put(fields[0], new double[] {
                    Double.parseDouble(fields[1]),
                    Double.parseDouble(fields[2]),
                    Double.parseDouble(fields[3]),
                    Double.parseDouble(fields[4])});
        }
    }

    private static int getSpeed(int level, int relativeSpeed) {
        int baseSpeed = 0;
        if (level >= 13) baseSpeed = 1;
        if (level >= 15) baseSpeed = 2;
        if (level >= 17) baseSpeed = 3;
        if (level >= 19) baseSpeed = 4;

        return baseSpeed + relativeSpeed;
    }

    private static int getStat(int stat, int level, Set<String> flags) {
        double base = 1.5 * level + 5;

        double scaleFactor = 1.0;
        for (String flag: flags) {
            if (modifiersMap.containsKey(flag)) {
                scaleFactor *= modifiersMap.get(flag)[stat];
            }
        }

        return (int) Math.round(scaleFactor * base);
    }

    private static int getExperience(int level, Set<String> flags, int constitution, int speed) {
        int hp = StatComputations.constitutionToHealth(constitution);
        double experience = hp/2.0;

        double scaleFactor = 1.0;
        if (flags.contains("erratic")) scaleFactor *= 0.7;
        if (flags.contains("stationary")) scaleFactor *= 0.5;
        // TODO: add spells in calculation

        double speedExpFactor = Math.pow(1.2, speed);

        return (int) Math.round(experience * scaleFactor * speedExpFactor);
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
            int level = Integer.parseInt(entry[0]);
            String id = entry[1];
            String genFlagsStr = entry[2];  // ideally, these will be removed, eventually made into good game flags
            String gameFlagsStr = entry[3];
            String spellFlagsStr = entry[4];
            int relativeSpeed = Integer.parseInt(entry[5]);
            String uniqueItemDrops = entry[6];
            int numDropChances = Integer.parseInt(entry[7]);
            String name = entry[8];
            String image = entry[9];
            String description = entry[10];

            Set<String> genFlags = getFlags(genFlagsStr);
            Set<String> gameFlags = getFlags(gameFlagsStr);
            Set<String> spellFlags = getFlags(spellFlagsStr);
            Set<String> flags = new HashSet<>();
            flags.addAll(genFlags);
            flags.addAll(gameFlags);
            flags.addAll(spellFlags);

            int speed = getSpeed(level, relativeSpeed);
            int strength = getStat(STR_INDEX, level, flags);
            int dexterity = getStat(DEX_INDEX, level, flags);
            int intelligence = getStat(INT_INDEX, level, flags);
            int constitution = getStat(CON_INDEX, level, flags);
            int experience = getExperience(level, flags, constitution, speed);
//            System.out.println(hp + "\t" + attackDieRoll + "\t" +
//                            attackDieRoll.roll * (1.0 + attackDieRoll.die) / 2.0
//                    + "\t" + attackAvg + "\t" + id);
//            System.out.println(level + "\t" + attackAvg + "\t" + defense + "\t" + toHit + "\t" + speed + "\t" + experience + "\t" + id);

//            int damage = StatComputations.STR_TO_DAMAGE.getPoints(strength);
//            int defense = StatComputations.DEX_TO_DEFENSE_AND_ATTACK.getPoints(dexterity);
//            int hp = StatComputations.CON_TO_HEALTH.getPoints(constitution);
//            int mp = StatComputations.INT_TO_MANA.getPoints(intelligence);

            writer.println(
                    level + "\t" +
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
