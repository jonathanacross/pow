package utils;

import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.gen.ItemGenerator;

import java.util.*;

// Program to display item stats by level to help with game balance.
public class ShowItemStats {

    // supposing that we find k random items of this slot, and take the best
    // what is the defense?
    public static int getSlotDefenseFromBestOfK(List<DungeonItem> items, int k, Random rng) {
        if (items == null || items.size() == 0) {
            return 0;
        }

        int bestDef = 0;
        for (int i = 0; i < k; i++) {
            DungeonItem item = items.get(rng.nextInt(items.size()));
            int totalDef = item.defense + item.bonuses[DungeonItem.DEF_IDX];
            if (totalDef > bestDef) {
                bestDef = totalDef;
            }
        }

        return bestDef;
    }

    public static void main(String[] args) {
        Random rng = new Random(123);

        for (int level = 0; level <= 90; level += 5) {

            // get list of items for this level
            List<String> itemIds = ItemGenerator.getItemIdsForLevel(level);
            List<DungeonItem> items = new ArrayList<>();
            for (int id = 0; id < itemIds.size(); id++) {
                items.add(ItemGenerator.genItem(itemIds.get(id), level, rng));
            }

            // group by slot
            Map<DungeonItem.Slot, List<DungeonItem>> itemsBySlot = new HashMap<>();
            for (DungeonItem item: items) {
                DungeonItem.Slot slot = item.slot;
                if (!itemsBySlot.containsKey(slot)) {
                    itemsBySlot.put(slot, new ArrayList<>());
                }
                itemsBySlot.get(slot).add(item);
            }

            DungeonItem.Slot[] defenseSlots = {
                    DungeonItem.Slot.ARMOR, DungeonItem.Slot.BOOTS,
                    DungeonItem.Slot.CLOAK, DungeonItem.Slot.GLOVES,
                    DungeonItem.Slot.HEADGEAR, DungeonItem.Slot.SHIELD };

            // get overall defense
            int randDefTotal = 0;
            int highDefTotal = 0;
            for (DungeonItem.Slot slot : defenseSlots) {
                randDefTotal += getSlotDefenseFromBestOfK(itemsBySlot.get(slot), 1, rng);
                highDefTotal += getSlotDefenseFromBestOfK(itemsBySlot.get(slot), 10, rng);
            }

            System.out.println(level + "\t" + randDefTotal + "\t" + highDefTotal);
        }
    }
}
