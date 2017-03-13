package pow.backend.dungeon.gen;

import pow.backend.AttackData;
import pow.backend.actors.Actor;
import pow.backend.actors.Monster;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.util.DebugLogger;
import pow.util.DieRoll;
import pow.util.Point;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MonsterGenerator {

    public static Set<String> getMonsterIds() {
        return instance.generatorMap.keySet();
    }

    // generates a single monster
    public static Monster genMonster(String id, Random rng, Point location) {
        if (!instance.generatorMap.containsKey(id)) {
            DebugLogger.error("unknown monster id '" + id + "'");
        }
        SpecificMonsterGenerator generator = instance.generatorMap.get(id);
        return generator.genMonster(rng, location);
    }

    private static final MonsterGenerator instance;
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
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/monsters.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        generatorMap = new HashMap<>();
        for (String[] line : reader.getData()) {
            SpecificMonsterGenerator smg = new SpecificMonsterGenerator(line);
            generatorMap.put(smg.id, smg);
        }
    }

    // helper class to generate a specific type of monster
    private static class SpecificMonsterGenerator {
        int level;
        String id;
        String name;
        String image;
        String description;
        int maxHealth;
        DieRoll attack;
        int toHit;
        int defense;
        int speed;
        AllFlags flags;
        int experience;
        String artifactDrops;  // slight misnomer.. can only handle 1 artifact right now
        int numDropAttempts;

        public static class AllFlags {
            final Monster.Flags aiActorFlags;
            final boolean friendly;

            public AllFlags(Monster.Flags aiActorFlags, boolean friendly) {
                this.aiActorFlags = aiActorFlags;
                this.friendly = friendly;
            }
        }

        private AllFlags parseFlags(String text) {
            String[] tokens = text.split(",", -1);

            boolean stationary = false;
            boolean erratic = false;
            boolean friendly = false;
            for (String t : tokens) {
                switch (t) {
                    case "": break;  // will happen if we have an empty string
                    case "stationary": stationary = true; break;
                    case "erratic": erratic = true; break;
                    case "knightmove": break;
                    case "boss": break;
                    case "friendly": friendly = true; break;
                    default:
                        throw new IllegalArgumentException("unknown monster flag '" + t + "'");
                }
            }

            return new AllFlags(new Monster.Flags(stationary, erratic), friendly);
        }

        private static String parseArtifact(String text) {
            if (text.isEmpty()) {
                return null;
            }

            // Validate that the artifact specified in the file actually exists
            DungeonItem checkArtifact = ArtifactData.getArtifact(text);
            if (checkArtifact == null) {
                DebugLogger.fatal(new RuntimeException("error: unknown artifact " + text));
            }
            return text;
        }

        // Parses the generator from text.
        // For now, assumes TSV, but may change this later.
        public SpecificMonsterGenerator(String[] line) {
            if (line.length != 14) {
                throw new IllegalArgumentException("Expected 14 fields, but had " + line.length
                + ". Fields = \n" + String.join(",", line));
            }

            level = Integer.parseInt(line[0]);
            id = line[1];
            name = line[2];
            image = line[3];
            description = line[4];
            maxHealth = Integer.parseInt(line[5]);
            attack = DieRoll.parseDieRoll(line[6]);
            toHit = Integer.parseInt(line[7]);
            defense = Integer.parseInt(line[8]);
            experience = Integer.parseInt(line[9]);
            speed = Integer.parseInt(line[10]);
            flags = parseFlags(line[11]);
            artifactDrops = parseArtifact(line[12]);
            numDropAttempts = Integer.parseInt(line[13]);
        }

        // resolves die rolls, location to get a specific monster instance
        public Monster genMonster(Random rng, Point location) {
            int instanceHP = maxHealth;
            AttackData attackData = new AttackData(attack, toHit, 0);
            return new Monster(
                    new DungeonObject.Params(id, name, image, description, location, true),
                    new Actor.Params(level, instanceHP, defense, experience, attackData,
                            flags.friendly, speed, artifactDrops, numDropAttempts),
                    flags.aiActorFlags);
        }
    }
}
