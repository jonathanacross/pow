package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.GameConstants;
import pow.backend.dungeon.DungeonItem;
import pow.backend.utils.ParseUtils;
import pow.util.DebugLogger;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ItemGenerator {

    public static Set<String> getItemIds() {
        return instance.generatorMap.keySet();
    }

    public static List<String> getSpecialItemIds() { return instance.specialItemIds; }

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

    // returns a list of possible gold drops
    public static List<String> getMoneyIdsForLevel(int level) {
        if (instance.levelToMoneyIds.containsKey(level)) {
            return instance.levelToMoneyIds.get(level);
        } else {
            if (level > instance.maxLevel) {
                return instance.levelToMoneyIds.get(instance.maxLevel);
            } else {
                return instance.levelToMoneyIds.get(instance.minLevel);
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

    private static final ItemGenerator instance;
    private Map<String, SpecificItemGenerator> generatorMap;
    private Map<Integer, List<String>> levelToItemIds;
    private List<String> specialItemIds;
    private Map<Integer, List<String>> levelToMoneyIds;
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
            // If an item has a negative level, don't allow it to be generated.
            // This way, unique, non-artifact items can be specified.
            minLevel = Math.min(minLevel, Math.max(sig.minLevel, 0));
            maxLevel = Math.max(maxLevel, sig.maxLevel);
        }

        // make the map of what items can be generated on each level,
        // since this is a 1-time computation.
        specialItemIds = new ArrayList<>();
        levelToItemIds = new HashMap<>();
        levelToMoneyIds = new HashMap<>();
        for (int level = minLevel; level <= maxLevel; level++) {
            levelToItemIds.put(level, new ArrayList<>());
            levelToMoneyIds.put(level, new ArrayList<>());
            for (SpecificItemGenerator sig : generatorMap.values()) {
                if (sig.minLevel <= level && level <= sig.maxLevel) {
                    levelToItemIds.get(level).add(sig.id);
                    if (sig.flags.money) {
                        levelToMoneyIds.get(level).add(sig.id);
                    }
                }
            }
        }
        for (SpecificItemGenerator sig : generatorMap.values()) {
            if (sig.minLevel < 0 && sig.maxLevel < 0) {
                specialItemIds.add(sig.id);
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
        int maxSockets;
        MinMax count; // number to generate
        int[] bonuses;

        public static class MinMax {
            public final int min;
            public final int max;

            public MinMax(String text) {
                if (text.isEmpty()) {
                    min = 1;
                    max = 1;
                    return;
                }

                String[] parts = text.split(":", 2);
                min = Integer.parseInt(parts[0]);
                max = Integer.parseInt(parts[1]);
            }
        }

        // Parses the generator from text.
        // For now, assumes TSV, but may change this later.
        public SpecificItemGenerator(String[] line) {
            if (line.length != 14) {
                throw new IllegalArgumentException("Expected 14 fields, but had " + line.length
                + ". Fields = \n" + String.join(",", line));
            }

            try {
                id = line[0];
                name = line[1];
                image = line[2];
                description = line[3];
                slot = DungeonItem.Slot.valueOf(line[4].toUpperCase());
                flags = ParseUtils.parseFlags(line[5]);
                actionParams = ParseUtils.parseActionParams(line[6]);
                count = new MinMax(line[7]);
                minLevel = Integer.parseInt(line[8]);
                maxLevel = Integer.parseInt(line[9]);
                minBonus = Integer.parseInt(line[10]);
                maxBonus = Integer.parseInt(line[11]);
                maxSockets = Integer.parseInt(line[12]);
                bonuses = ParseUtils.parseBonuses(line[13]);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(e.getMessage() + "\nFields = \n" + String.join(",", line), e);
            }

            if (maxLevel < minLevel) {
                throw new IllegalArgumentException("maxLevel < minLevel. Fields = \n" + String.join(",", line));
            }
        }

        // redistributes the points in 'fromIdx' randomly across the stats in 'toIndices'.
        private void distributeMetaBonuses(int[] bonuses, int fromIdx, int[] toIndices, Random rng) {
            while (bonuses[fromIdx] > 0) {
                int toIdx = toIndices[rng.nextInt(toIndices.length)];
                bonuses[toIdx]++;
                bonuses[fromIdx]--;
            }
        }

        private int getMoneyAmountForLevel(double level, Random rng) {
            int maxAmt = (int) (0.3 * level * level + 10);
            return rng.nextInt(maxAmt) + 1;
        }

        // resolves level to get a specific item instance
        public DungeonItem genItem(double level, Random rng) {
            // compute general bonus given the level
            double ratio = (level - minLevel) / (double) (maxLevel - minLevel);
            int bonus = (int) Math.round(ratio * (maxBonus - minBonus) + minBonus);

            // convert to item bonuses
            int[] specificItemBonuses = new int[DungeonItem.NUM_BONUSES];
            for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
                specificItemBonuses[i] = (bonuses[i] == ParseUtils.MATCH_BONUS) ? bonus : bonuses[i];
            }
            distributeMetaBonuses(specificItemBonuses, DungeonItem.ATTACK_IDX,
                    new int[]{DungeonItem.TO_HIT_IDX, DungeonItem.TO_DAM_IDX}, rng);
            distributeMetaBonuses(specificItemBonuses, DungeonItem.COMBAT_IDX,
                    new int[]{DungeonItem.TO_HIT_IDX, DungeonItem.TO_DAM_IDX, DungeonItem.DEF_IDX}, rng);
            distributeMetaBonuses(specificItemBonuses, DungeonItem.RESIST_IDX,
                    new int[]{DungeonItem.RES_ACID_IDX,
                            DungeonItem.RES_COLD_IDX,
                            DungeonItem.RES_FIRE_IDX,
                            DungeonItem.RES_ELEC_IDX,
                            DungeonItem.RES_POIS_IDX,
                            DungeonItem.RES_DAM_IDX}, rng);
            distributeMetaBonuses(specificItemBonuses, DungeonItem.ABILITY_IDX,
                    new int[]{DungeonItem.STR_IDX, DungeonItem.INT_IDX,
                            DungeonItem.DEX_IDX, DungeonItem.CON_IDX},
                    rng);

            // add sockets with some (fairly low) probability, according to a geometric distribution
            int numSockets = (int) Math.floor(Math.log(rng.nextDouble()) / Math.log(GameConstants.PROB_GEN_SOCKET));

            numSockets = Math.min(numSockets, maxSockets);
            specificItemBonuses[DungeonItem.SOCKETS_IDX] = numSockets;

            int itemCount = flags.money
                    ? getMoneyAmountForLevel(level, rng)
                    : rng.nextInt(count.max + 1 - count.min) + count.min;

            return new DungeonItem(id, name, image, description, slot, DungeonItem.ArtifactSlot.NONE,
                    flags, specificItemBonuses, itemCount, actionParams);
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
