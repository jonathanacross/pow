package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonItem;
import pow.util.DebugLogger;
import pow.util.DieRoll;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ItemGenerator {

    public static Set<String> getItemIds() {
        return instance.generatorMap.keySet();
    }

    public static List<String> getItemIdsForLevel(int level) {
        if (instance.levelToItemIds.containsKey(level)) {
            return instance.levelToItemIds.get(level);
        } else {
            if (level > instance.maxLevel) {
                return instance.levelToItemIds.get(instance.maxLevel);
            } else {
                return instance.levelToItemIds.get(instance.minLevel);
            }
        }
    }

    // generates a single item
    public static DungeonItem genItem(String id, double level, Random rng) {
        if (!instance.generatorMap.containsKey(id)) {
            DebugLogger.error("unknown item id '" + id + "'");
        }
        SpecificItemGenerator generator = instance.generatorMap.get(id);
        return generator.genItem(level, rng);
    }

    public static ItemGenerator instance;
    private Map<String, SpecificItemGenerator> generatorMap;
    private Map<Integer, List<String>> levelToItemIds;
    private int minLevel;
    private int maxLevel;

    static {
        try {
            instance = new ItemGenerator();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private ItemGenerator() throws IOException {
        // Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/items.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        generatorMap = new HashMap<>();
        for (String[] line : reader.getData()) {
            SpecificItemGenerator smg = new SpecificItemGenerator(line);
            generatorMap.put(smg.id, smg);
        }

        // determine min/max levels for all items.
        minLevel = Integer.MAX_VALUE;
        maxLevel = 0;
        for (SpecificItemGenerator sig : generatorMap.values()) {
            minLevel = Math.min(minLevel, sig.minLevel);
            maxLevel = Math.max(maxLevel, sig.maxLevel);
        }

        // make the map of what items can be generated on each level,
        // since this is a 1-time computation.
        levelToItemIds = new HashMap<>();
        for (int level = minLevel; level <= maxLevel; level++) {
            levelToItemIds.put(level, new ArrayList<>());
            for (SpecificItemGenerator sig : generatorMap.values()) {
                if (sig.minLevel <= level && level <= sig.maxLevel) {
                    levelToItemIds.get(level).add(sig.id);
                }
            }
        }
    }

    // helper class to generate a specific type of item
    private static class SpecificItemGenerator {
        String id;
        String name;
        String image;
        String description;
        DungeonItem.Slot slot;
        DungeonItem.Flags flags;
        ActionParams actionParams;
        int minLevel;
        int maxLevel;
        int minBonus;
        int maxBonus;
        int[] bonuses;
        DieRoll attack;
        int defense;
        String extra;

        private DungeonItem.Flags parseFlags(String text) {
            String[] tokens = text.split(",", -1);

            boolean potion = false;
            for (String t : tokens) {
                switch (t) {
                    case "": break;  // will happen if we have an empty string
                    case "potion": potion = true; break;
                    default:
                        throw new IllegalArgumentException("unknown item flag '" + t + "'");
                }
            }

            return new DungeonItem.Flags(potion);
        }

        // TODO: duplicate code in TerrainData
        private ActionParams parseActionParams(String text) {
            ActionParams params = new ActionParams();
            if (text.isEmpty()) {
                return params;
            }
            String[] tokens = text.split(":", 3);

            params.actionName = tokens[0];

            if (!tokens[1].isEmpty()) {
                params.number = Integer.parseInt(tokens[1]);
            }

            if (!tokens[2].isEmpty()) {
                params.name = tokens[2];
            }

            return params;
        }

        private static final int ATTACK_IDX = DungeonItem.TO_HIT_IDX;
        private static final int MATCH_BONUS = -99999;
        private static final Map<String, Integer> keyToBonusIdx;
        static {
            keyToBonusIdx = new HashMap<>();
            keyToBonusIdx.put("attack", ATTACK_IDX);
            keyToBonusIdx.put("def", DungeonItem.DEF_IDX);
            keyToBonusIdx.put("str", DungeonItem.STR_IDX);
            keyToBonusIdx.put("dex", DungeonItem.DEX_IDX);
            keyToBonusIdx.put("int", DungeonItem.INT_IDX);
            keyToBonusIdx.put("con", DungeonItem.CON_IDX);
            keyToBonusIdx.put("speed", DungeonItem.SPEED_IDX);
        }

        private int[] parseBonuses(String text) {
            int[] bonuses = new int[DungeonItem.NUM_BONUSES];
            if (text.isEmpty()) {
                return bonuses;
            }

            String[] statBonuses = text.split(",");
            for (String statBonus: statBonuses) {
                String[] tokens = statBonus.split(":", 2);
                // x indicates that we'll use the bounus calculated based on the level
                int bonusAmt = (tokens[1].charAt(0) == 'x') ? MATCH_BONUS : Integer.parseInt(tokens[1]);
                String stat = tokens[0];
                if (keyToBonusIdx.containsKey(stat)) {
                    bonuses[keyToBonusIdx.get(stat)] = bonusAmt;
                } else {
                    throw new RuntimeException("error: couldn't parse item stat bonus: " + stat);
                }
            }

            return bonuses;
        }

        // Parses the generator from text.
        // For now, assumes TSV, but may change this later.
        public SpecificItemGenerator(String[] line) {
            if (line.length != 15) {
                throw new IllegalArgumentException("Expected 15 fields, but had " + line.length
                + ". Fields = \n" + String.join(",", line));
            }
            id = line[0];
            name = line[1];
            image = line[2];
            description = line[3];
            slot = DungeonItem.Slot.valueOf(line[4].toUpperCase());
            flags = parseFlags(line[5]);
            actionParams = parseActionParams(line[6]);
            minLevel = Integer.parseInt(line[7]);
            maxLevel = Integer.parseInt(line[8]);
            minBonus = Integer.parseInt(line[9]);
            maxBonus = Integer.parseInt(line[10]);
            bonuses = parseBonuses(line[11]);
            attack = DieRoll.parseDieRoll(line[12]);
            defense = Integer.parseInt(line[13]);
            extra = line[14];

            if (maxLevel < minLevel) {
                throw new IllegalArgumentException("maxLevel < minLevel. Fields = \n" + String.join(",", line));
            }
        }

        // resolves level to get a specific item instance
        // TODO: add quantity (e.g., for arrows, money)
        public DungeonItem genItem(double level, Random rng) {
            // compute general bonus given the level
            double ratio = (level - minLevel) / (double) (maxLevel - minLevel);
            int bonus = (int) Math.round(ratio * (maxBonus - minBonus) + minBonus);

            // convert to item bonuses
            int[] specificItemBonuses = new int[DungeonItem.NUM_BONUSES];
            for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
                specificItemBonuses[i] = (bonuses[i] == MATCH_BONUS) ? bonus : bonuses[i];
            }
            // split up attack into toHit, toDam.
            int totalAttack = specificItemBonuses[ATTACK_IDX];
            specificItemBonuses[DungeonItem.TO_HIT_IDX] = 0;
            specificItemBonuses[DungeonItem.TO_DAM_IDX] = 0;
            if (totalAttack > 0) {
                specificItemBonuses[DungeonItem.TO_HIT_IDX] = rng.nextInt(totalAttack);
                specificItemBonuses[DungeonItem.TO_DAM_IDX] = totalAttack - specificItemBonuses[DungeonItem.TO_HIT_IDX];
            }

            return new DungeonItem(name, image, description, slot, flags, specificItemBonuses, attack,
                    defense, 1, actionParams);
        }
    }

    public static void main(String[] args) {
        Random rng = new Random(123);
        for (int level = 0; level < 100; level += 5) {
            List<String> itemIds = ItemGenerator.getItemIdsForLevel(level);
            System.out.println(level);
            for (String itemId : itemIds) {
                DungeonItem item = ItemGenerator.genItem(itemId, level, rng);
                System.out.println("\t" + item.name);
            }
        }
    }
}
