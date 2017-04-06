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

    // monsters that can walk on ground
    public static Set<String> getGroundMonsterIds() { return instance.groundMonsterIds; }

    // monsters that can swim in the water
    public static Set<String> getWaterMonsterIds() { return instance.waterMonsterIds; }

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
    private Set<String> groundMonsterIds;
    private Set<String> waterMonsterIds;

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
        groundMonsterIds = new HashSet<>();
        waterMonsterIds = new HashSet<>();
        for (String[] line : reader.getData()) {
            SpecificMonsterGenerator smg = new SpecificMonsterGenerator(line);
            generatorMap.put(smg.id, smg);
            if (smg.flags.aquatic) {
                waterMonsterIds.add(smg.id);
            } else {
                groundMonsterIds.add(smg.id);
            }
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
        int maxMana;
        DieRoll attack;
        int toHit;
        int defense;
        int speed;
        AllFlags flags;
        int experience;
        List<Monster.Spell> spells;
        String artifactDrops;  // slight misnomer.. can only handle 1 artifact right now
        int numDropAttempts;

        public static class AllFlags {
            final Monster.Flags monsterFlags;
            final boolean friendly;
            final boolean invisible;
            final boolean aquatic;

            public AllFlags(Monster.Flags monsterFlags, boolean friendly, boolean invisible, boolean aquatic) {
                this.monsterFlags = monsterFlags;
                this.friendly = friendly;
                this.invisible = invisible;
                this.aquatic = aquatic;
            }
        }

        private List<Monster.Spell> parseSpells(String text) {
            List<Monster.Spell> spells = new ArrayList<>();

            String[] tokens = text.split(",", -1);
            for (String token : tokens) {
                if (token.isEmpty()) continue; // will happen for empty string

                Monster.Spell spell = Monster.Spell.valueOf(token.toUpperCase());
                spells.add(spell);
            }
            return spells;
        }

        private AllFlags parseFlags(String text) {
            String[] tokens = text.split(",", -1);

            boolean stationary = false;
            boolean erratic = false;
            boolean friendly = false;
            boolean invisible = false;
            boolean aquatic = false;
            boolean knightmove = false;
            boolean aggressive = false;
            boolean passive = false;
            boolean perfect = false;
            for (String t : tokens) {
                switch (t) {
                    case "": break;  // will happen if we have an empty string
                    case "stationary": stationary = true; break;
                    case "erratic": erratic = true; break;
                    case "knightmove": knightmove = true; break;
                    case "boss": break;
                    case "friendly": friendly = true; break;
                    case "invisible": invisible = true; break;
                    case "aquatic": aquatic = true; break;
                    case "aggressive": aggressive = true; break;
                    case "passive": passive = true; break;
                    case "perfect": perfect = true; break;
                    default:
                        throw new IllegalArgumentException("unknown monster flag '" + t + "'");
                }
            }

            return new AllFlags(new Monster.Flags(stationary, erratic, knightmove, aggressive, passive, perfect),
                    friendly, invisible, aquatic);
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
            if (line.length != 16) {
                throw new IllegalArgumentException("Expected 16 fields, but had " + line.length
                + ". Fields = \n" + String.join(",", line));
            }

            level = Integer.parseInt(line[0]);
            id = line[1];
            name = line[2];
            image = line[3];
            description = line[4];
            maxHealth = Integer.parseInt(line[5]);
            maxMana = Integer.parseInt(line[6]);
            attack = DieRoll.parseDieRoll(line[7]);
            toHit = Integer.parseInt(line[8]);
            defense = Integer.parseInt(line[9]);
            experience = Integer.parseInt(line[10]);
            speed = Integer.parseInt(line[11]);
            flags = parseFlags(line[12]);
            spells = parseSpells(line[13]);
            artifactDrops = parseArtifact(line[14]);
            numDropAttempts = Integer.parseInt(line[15]);
        }

        // resolves die rolls, location to get a specific monster instance
        public Monster genMonster(Random rng, Point location) {
            AttackData attackData = new AttackData(attack, toHit, 0);
            return new Monster(
                    new DungeonObject.Params(id, name, image, description, location, true),
                    new Actor.Params(level, maxHealth, maxMana,
                            defense, experience, attackData,
                            flags.friendly, flags.invisible, flags.aquatic, speed,
                            artifactDrops, numDropAttempts, spells),
                    flags.monsterFlags);
        }
    }
}
