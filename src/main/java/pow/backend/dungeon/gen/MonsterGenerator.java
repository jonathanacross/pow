package pow.backend.dungeon.gen;

import pow.backend.actors.AiActor;
import pow.backend.actors.Monster;
import pow.util.DebugLogger;
import pow.util.Point;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MonsterGenerator {

    // generates a single monster
    public static Monster genMonster(String id, Random rng, Point location) {
        if (!instance.generatorMap.containsKey(id)) {
            DebugLogger.error("unknown monster id '" + id + "'");
        }
        SpecificMonsterGenerator generator = instance.generatorMap.get(id);
        return generator.genMonster(rng, location);
    }

    public static MonsterGenerator instance;
    private Map<String, SpecificMonsterGenerator> generatorMap;

    static {
        try {
            instance = new MonsterGenerator();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private MonsterGenerator() throws IOException {
        //Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/monsters.txt");
        TsvReader reader = new TsvReader(tsvStream);

        generatorMap = new HashMap<>();
        for (String[] line : reader.getData()) {
            SpecificMonsterGenerator smg = new SpecificMonsterGenerator(line);
            generatorMap.put(smg.id, smg);
        }
    }

    // helper class to generate a specific type of monster
    private static class SpecificMonsterGenerator {
        String id;
        String name;
        String image;
        String description;
        int maxHealth;   // TODO: make into a die roll
        //List<dierolls> attacks
        int speed;
        AiActor.Flags flags;
        int experience;

        private AiActor.Flags parseFlags(String text) {
            String[] tokens = text.split(",", -1);

            boolean stationary = false;
            boolean erratic = false;
            for (String t : tokens) {
                switch (t) {
                    case "": break;  // will happen if we have an empty string
                    case "stationary": stationary = true; break;
                    case "erratic": erratic = true; break; default:
                        throw new IllegalArgumentException("unknown monster flag '" + t + "'");
                }
            }

            return new AiActor.Flags(stationary, erratic);
        }

        // Parses the generator from text.
        // For now, assumes TSV, but may change this later.
        public SpecificMonsterGenerator(String[] line) {
            if (line.length != 9) {
                throw new IllegalArgumentException("expected line to have 9 fields");
            }

            id = line[0];
            name = line[1];
            image = line[2];
            description = line[3];
            maxHealth = Integer.parseInt(line[4]);
            // attacks = parseAttacks(line[5]);
            speed = Integer.parseInt(line[6]);
            flags = parseFlags(line[7]);
            experience = Integer.parseInt(line[8]);
        }

        // resolves die rolls, location to get a specific monster instance
        public Monster genMonster(Random rng, Point location) {
            return new Monster(id, name, image, description, maxHealth, speed, location.x, location.y, flags);
        }
    }
}
