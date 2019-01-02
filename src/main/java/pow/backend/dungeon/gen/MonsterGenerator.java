package pow.backend.dungeon.gen;

import pow.backend.SpellParams;
import pow.backend.actors.*;
import pow.backend.actors.ai.KnightMovement;
import pow.backend.actors.ai.Movement;
import pow.backend.actors.ai.StationaryMovement;
import pow.backend.actors.ai.StepMovement;
import pow.backend.dungeon.DungeonObject;
import pow.util.DebugLogger;
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
    public static Monster genMonster(String id, Random rng, boolean perturb, Point location) {
        if (!instance.generatorMap.containsKey(id)) {
            DebugLogger.error("unknown monster id '" + id + "'");
        }
        SpecificMonsterGenerator generator = instance.generatorMap.get(id);
        return generator.genMonster(rng, perturb, location);
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
        // Get file from resources folder
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
        int strength;
        int dexterity;
        int intelligence;
        int constitution;
        int speed;
        AllFlags flags;
        int experience;
        List<SpellParams> spells;
        List<String> artifactDrops;
        int numDropAttempts;
        Movement movement;

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

        private List<SpellParams> parseSpells(String text) {
            List<SpellParams> spells = new ArrayList<>();

            String[] tokens = text.split(",", -1);
            for (String token : tokens) {
                if (token.isEmpty()) continue; // will happen for empty string

                SpellParams spell = SpellData.getSpell(token);
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
            boolean fearless = false;
            boolean passive = false;
            boolean perfect = false;
            for (String t : tokens) {
                switch (t) {
                    case "": break;  // will happen if we have an empty string
                    case "stationary": stationary = true; break;
                    case "knightmove": knightmove = true; break;
                    case "boss": break;
                    case "friendly": friendly = true; break;
                    case "invisible": invisible = true; break;
                    case "aquatic": aquatic = true; break;
                    case "fearless": fearless = true; break;
                    case "passive": passive = true; break;
                    case "erratic": erratic = true; break;
                    case "perfect": perfect = true; break;
                    default:
                        throw new IllegalArgumentException("unknown monster flag '" + t + "'");
                }
            }

            // make sure there aren't incompatible flags..
            if (erratic && passive) {
                throw new RuntimeException("error: saw flags: " + text +
                        "; only one of {erratic, passive} allowed");
            }

            if (erratic && fearless) {
                throw new RuntimeException("error: saw flags: " + text +
                        "; only one of {erratic, fearless} allowed");
            }

            if (erratic && perfect) {
                throw new RuntimeException("error: saw flags: " + text +
                        "; only one of {erratic, perfect} allowed");
            }

            if (erratic && stationary) {
                throw new RuntimeException("error: saw flags: " + text +
                        "; only one of {erratic, stationary} allowed");
            }

            if (stationary && knightmove) {
                throw new RuntimeException("error: saw flags: " + text +
                        "; only one of {stationary, knightmove} allowed");
            }

            return new AllFlags(new Monster.Flags(stationary, erratic, knightmove, fearless, passive, perfect),
                    friendly, invisible, aquatic);
        }

        private static Set<String> getFlags(String field) {
            String[] fields = field.split(",", -1);
            Set<String> flags = new HashSet<>();
            for (String tok : fields) {
                if (!tok.isEmpty()) {
                    flags.add(tok);
                }
            }
            return flags;
        }

        private static List<String> parseArtifacts(String text) {
            if (text.isEmpty()) {
                return Collections.emptyList();
            }
            String[] itemIds = text.split(",", -1);
            List<String> result = new ArrayList<>();
            for (String itemId : itemIds) {
                boolean isSpecialItem = ItemGenerator.getSpecialItemIds().contains(itemId);
                if (!isSpecialItem && ArtifactData.getArtifact(itemId) == null) {
                    DebugLogger.fatal(new RuntimeException("error: unknown artifact/special item " + itemId));
                }
                result.add(itemId);
            }
            return result;
        }

        private static int getSpeed(int level, int relativeSpeed) {
            int baseSpeed = 0;
            if (level >= 13) baseSpeed = 1;
            if (level >= 15) baseSpeed = 2;
            if (level >= 17) baseSpeed = 3;
            if (level >= 19) baseSpeed = 4;

            return baseSpeed + relativeSpeed;
        }

        private enum GainRatioStats { STRENGTH, DEXTERITY, INTELLIGENCE, CONSTITUTION }
        private static int getStat(GainRatioStats whichStat, int level, Set<String> flags) {
            double value = 1.5 * level + 5;

            for (String flag: flags) {
                GainRatios gainRatios = GainRatiosData.getGainRatios(flag);
                switch (whichStat) {
                    case STRENGTH: value *= gainRatios.strRatio; break;
                    case DEXTERITY: value *= gainRatios.dexRatio; break;
                    case INTELLIGENCE: value *= gainRatios.intRatio; break;
                    case CONSTITUTION: value *= gainRatios.conRatio; break;
                    default:
                }
            }

            return (int) Math.round(value);
        }

        // Simple heuristic to quantify how deadly a spell is.
        // Currently just based on the type of spell; not taking into
        // account the element.
        private static double getSpellExperienceFactor(SpellParams spell) {
            double typeFactor = 1.0;
            switch (spell.spellType) {
                // spells that the monster can cast affecting large areas are particularly deadly
                case QUAKE: typeFactor = 1.3; break;
                case BALL: typeFactor = 1.2; break;
                case CHAIN: typeFactor = 1.2; break;
                case BREATH: typeFactor = 1.15; break;

                // spells that just require line of sight
                case ARROW:
                case BOLT:
                case CIRCLE_CUT: typeFactor = 1.1; break;

                // these don't really make the monster harder/easier.
                case BOOST_ARMOR:
                case BOOST_ATTACK:
                case HEAL:
                case GROUP_HEAL:
                case RESIST_ELEMENTS:
                case SPEED: typeFactor = 1.0; break;

                // this actually makes things easier since it doesn't directly attack the player
                // and dilutes other more deadly spells
                case CALL_PET:
                case PHASE: typeFactor = 0.8; break;
            }
            return typeFactor;
        }

        private static double getSpellListExperienceFactor(List<SpellParams> spells) {
            if (spells.isEmpty()) return 1.0;
            double total = 0.0;
            for (SpellParams spell : spells) {
                total += getSpellExperienceFactor(spell);
            }
            return total / spells.size();
        }

        private static int getExperience(Set<String> flags, int constitution, int dexterity, int strength, int speed, List<SpellParams> spells) {
            int hp = StatComputations.constitutionToHealth(constitution);
            int damage = StatComputations.strengthToDamage(strength);
            int agility = StatComputations.dexterityToDefenseAndAttack(dexterity);
            double experience = Math.pow((double) hp * damage * agility, 1.0/3.0);

            double scaleFactor = 1.0;
            if (flags.contains("erratic")) scaleFactor *= 0.7;
            if (flags.contains("stationary")) scaleFactor *= 0.5;
            scaleFactor *= getSpellListExperienceFactor(spells);

            double speedExpFactor = Math.pow(1.2, speed);

            return (int) Math.round(experience * scaleFactor * speedExpFactor);
        }


        // Parses the generator from text.
        // For now, assumes TSV, but may change this later.
        public SpecificMonsterGenerator(String[] line) {
            if (line.length != 11) {
                throw new IllegalArgumentException("Expected 11 fields, but had " + line.length
                + ". Fields = \n" + String.join(",", line));
            }

            level = Integer.parseInt(line[0]);
            id = line[1];
            String genFlagsStr = line[2];
            String gameFlagsStr = line[3];
            String spellFlagsStr = line[4];
            int relativeSpeed = Integer.parseInt(line[5]);
            artifactDrops = parseArtifacts(line[6]);
            numDropAttempts = Integer.parseInt(line[7]);
            name = line[8];
            image = line[9];
            description = line[10];

            Set<String> genFlags = getFlags(genFlagsStr);
            Set<String> gameFlags = getFlags(gameFlagsStr);
            Set<String> spellFlags = getFlags(spellFlagsStr);
            Set<String> allGeneratorFlags = new HashSet<>();
            allGeneratorFlags.addAll(genFlags);
            allGeneratorFlags.addAll(gameFlags);
            allGeneratorFlags.addAll(spellFlags);

            flags = parseFlags(gameFlagsStr);
            spells = parseSpells(spellFlagsStr);
            speed = getSpeed(level, relativeSpeed);
            strength = getStat(GainRatioStats.STRENGTH, level, allGeneratorFlags);
            dexterity = getStat(GainRatioStats.DEXTERITY, level, allGeneratorFlags);
            intelligence = getStat(GainRatioStats.INTELLIGENCE, level, allGeneratorFlags);
            constitution = getStat(GainRatioStats.CONSTITUTION, level, allGeneratorFlags);
            experience = getExperience(allGeneratorFlags, constitution, dexterity, strength, speed, spells);
            movement = flags.monsterFlags.stationary ? new StationaryMovement() :
                            (flags.monsterFlags.knight ? new KnightMovement() : new StepMovement());
        }

        private static class BaseStats {
            private final int[] stats;

            public BaseStats(int strength, int dexterity, int intelligence, int constitution) {
                stats = new int[] {strength, dexterity, intelligence, constitution};
            }

            public void perturb(Random rng) {
                int sum = stats[0] + stats[1] + stats[2] + stats[3];
                int numPerturbs = sum / 15;

                for (int i = 0; i < numPerturbs; i++) {
                    int idx1 = rng.nextInt(4);
                    int idx2 = rng.nextInt(4);
                    stats[idx1]--;
                    stats[idx2]++;
                }
            }

            public int getStr() { return stats[0]; }
            public int getDex() { return stats[1]; }
            public int getInt() { return stats[2]; }
            public int getCon() { return stats[3]; }
        }

        // resolves die rolls, location to get a specific monster instance
        public Monster genMonster(Random rng, boolean perturb, Point location) {
            // add a little variability to the monster's stats
            BaseStats baseStats = new BaseStats(strength, dexterity, intelligence, constitution);
            if (perturb) {
                baseStats.perturb(rng);
            }

            return new Monster(
                    new DungeonObject.Params(id, name, image, description, location, true),
                    new Actor.Params(level, experience, flags.friendly, flags.invisible,
                            flags.aquatic, movement, artifactDrops, numDropAttempts,
                            baseStats.getStr(), baseStats.getDex(), baseStats.getInt(), baseStats.getCon(),
                            speed, spells, new Abilities()),
                    flags.monsterFlags);
        }
    }
}
