package pow.backend.ai;

import pow.backend.GameState;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.ItemList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Class to optimize current equipment/inventory based on current holdings
// and what's on the ground.  Note that this is mostly designed for player
// optimization, as it only replaces equipment that is strictly better than
// current used equipment, rather than trying to find best global combinations
// of equipment for computer AI.
// Specifically, the resulting set of equipment/inventory will have
// all items strictly better than or equal to the current set of items
// (in every bonus).  Any redundant items are dropped.
// For example, given
//     Equipment: sword +0, +0, +1
//     Inventory: sword +0, +0, +1
//     Inventory: sword +0, +1, +0
//     Inventory: sword +1, +2, +0
//     Ground:    sword +0, +0, +0
//     Ground:    sword +0, +0, +2
// This will find that the item that is strictly >= the current equipment
// is the sword +0, +0, +2.  All other items in inventory are worse than
// this, except for the sword +1, +2, +0, which is not better in the 3rd
// bonus.  This item is still kept in inventory.  All others are dropped on
// the ground.
public class AutoItem {

    public enum Location {
        EQUIPMENT(0),
        INVENTORY(1),
        GROUND(2);

        private final int rank;

        Location(int rank) {
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }
    }

    public static class ItemMovement {
        public final DungeonItem item;
        public final Location from;
        public final Location to;

        public ItemMovement(DungeonItem item, Location from, Location to) {
            this.item = item;
            this.from = from;
            this.to = to;
        }
    }

    public static List<ItemMovement> optimizeItems(GameState gs) {
        Player player = gs.party.player;
        DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
        List<DungeonItem> ground = square.items.items;
        List<DungeonItem> inventory = player.inventory.items;
        List<DungeonItem> equipment = player.equipment.items;

        List<ItemMovement> result = new ArrayList<>();
        for (DungeonItem.Slot slot : DungeonItem.Slot.values()) {
            if (slot == DungeonItem.Slot.NONE) {
                continue;
            }

            // go through each slot and optimize independently
            List<ItemLoc> itemLocs = new ArrayList<>();

            for (DungeonItem item : equipment) {
                if (item.slot != slot) {
                    continue;
                }
                itemLocs.add(new ItemLoc(item, Location.EQUIPMENT));
            }
            for (DungeonItem item : inventory) {
                if (item.slot != slot) {
                    continue;
                }
                itemLocs.add(new ItemLoc(item, Location.INVENTORY));
            }
            for (DungeonItem item : ground) {
                if (item.slot != slot) {
                    continue;
                }
                itemLocs.add(new ItemLoc(item, Location.GROUND));
            }

            result.addAll(optimizeItems(itemLocs));
        }

        result.addAll(pickUpDuplicateItems(gs));
        result.addAll(pickUpImportantItems(gs));

        return result;
    }

    // Needed for the corner case where we pick up a special item -- we might
    // optimize to put these somewhere already, and then try to pick up again
    // in pickUpImportantItemts.
    private static boolean alreadyMoved(ItemMovement candidate, List<ItemMovement> movements) {
        for (ItemMovement movement : movements) {
            if (movement.from == candidate.from && movement.item == candidate.item) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemMovement> simplifyMovements(List<ItemMovement> movements) {
        List<ItemMovement> result = new ArrayList<>();

        // Do drop actions first
        for (ItemMovement movement : movements) {
            if (movement.to == Location.GROUND && movement.from != Location.GROUND && !alreadyMoved(movement, result)) {
                result.add(movement);
            }
        }
        // Next wear items
        for (ItemMovement movement : movements) {
            if (movement.to == Location.EQUIPMENT && movement.from != Location.EQUIPMENT && !alreadyMoved(movement, result)) {
                result.add(movement);
            }
        }
        // Finally pick up.
        for (ItemMovement movement : movements) {
            if (movement.to == Location.INVENTORY && movement.from != Location.INVENTORY && !alreadyMoved(movement, result)) {
                result.add(movement);
            }
        }

        return result;
    }

    private static class ItemLoc {
        final DungeonItem item;
        final Location loc;

        public ItemLoc(DungeonItem item, Location loc) {
            this.item = item;
            this.loc = loc;
        }

        @Override
        public String toString() {
            return this.item.stringWithInfo() + "@" + this.loc;
        }
    }

    private static class UsefulAndUselessItems {
        final List<ItemLoc> useful;
        final List<ItemLoc> useless;

        public UsefulAndUselessItems(List<ItemLoc> useful, List<ItemLoc> useless) {
            this.useful = useful;
            this.useless = useless;
        }
    }

    private static boolean isBetterOrEqual(ItemLoc a, ItemLoc b) {
        // Only applies to wearable items; potions, gems are skipped.
        if (a.item.slot == DungeonItem.Slot.NONE) {
            return false;
        }
        // Items must be comparable
        if (a.item.slot != b.item.slot) {
            return false;
        }

        // compare bonuses
        for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
            if (a.item.bonuses[i] < b.item.bonuses[i]) {
                // a is worse than b in some bonus
                return false;
            }
        }
        return true;
    }

    // Sees if a is strictly better than b.
    private static boolean isBetter(ItemLoc a, ItemLoc b) {
        // Only applies to wearable items; potions, gems are skipped.
        if (a.item.slot == DungeonItem.Slot.NONE) {
            return false;
        }
        // Items must be comparable
        if (a.item.slot != b.item.slot) {
            return false;
        }

        // first, compare bonuses
        boolean aIsStrictlyBetter = false;
        for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
            if (a.item.bonuses[i] < b.item.bonuses[i]) {
                // a is worse than b in some bonus
                return false;
            } else if (a.item.bonuses[i] > b.item.bonuses[i]) {
                aIsStrictlyBetter = true;
            }
        }
        if (aIsStrictlyBetter) {
            return true;
        }

        // items have same bonus.  Look at location to break the tie.
        if (a.loc.getRank() < b.loc.getRank()) {
            return true;
        } else if (a.loc.getRank() > b.loc.getRank()) {
            return false;
        }

        // items have same bonus and are both in same location.
        // use id to break the tie.
        return (a.item.id).compareTo(b.item.id) < 0;
    }


    // Finds items that are worse than other items; these must belong on the ground.
    private static UsefulAndUselessItems classifyItems(List<ItemLoc> itemLocs) {
        List<ItemLoc> useful = new ArrayList<>();
        List<ItemLoc> useless = new ArrayList<>();

        for (ItemLoc a : itemLocs) {
            boolean existsBetterItemThanA = false;
            for (ItemLoc b : itemLocs) {
                if (a == b) {
                    continue;
                }
                if (isBetter(b, a)) {
                    existsBetterItemThanA = true;
                }
            }
            if (!existsBetterItemThanA) {
                useful.add(a);
            } else {
                useless.add(a);
            }
        }

        return new UsefulAndUselessItems(useful, useless);
    }

    private static List<ItemMovement> optimizeItems(List<ItemLoc> itemLocs) {
        // handle some degenerate, simple cases
        List<ItemMovement> result = new ArrayList<>();
        if (itemLocs.isEmpty()) {
            return result;
        }

        if (itemLocs.size() == 1) {
            // Only one item; best place to put it is to wear it.
            result.add(new ItemMovement(itemLocs.get(0).item, itemLocs.get(0).loc, Location.EQUIPMENT));
            return result;
        }

        // Now the real work.

        // First step: remove items that are clearly bad.
        UsefulAndUselessItems classifiedItems = classifyItems(itemLocs);
        for (ItemLoc i : classifiedItems.useless) {
            result.add(new ItemMovement(i.item, i.loc, Location.GROUND));
        }

        // If equipment is empty, then pick the overall best one for equipment.
        ItemLoc equipment = getEquipmentItem(itemLocs);
        if (equipment == null) {
            equipment = getBestOverallItem(classifiedItems.useful);
        }
        ItemLoc bestEquipment = getBestMajorizingItem(equipment, classifiedItems.useful);
        result.add(new ItemMovement(bestEquipment.item, bestEquipment.loc, Location.EQUIPMENT));
        classifiedItems.useful.remove(bestEquipment);  // don't have to consider this one any more

        // try to find majorizing items for remaining inventory items
        List<ItemLoc> currentInventory = getInventoryItems(bestEquipment, itemLocs);
        for (ItemLoc invItem : currentInventory) {
            ItemLoc bestInv = getBestMajorizingItem(invItem, classifiedItems.useful);
            result.add(new ItemMovement(bestInv.item, bestInv.loc, Location.INVENTORY));
            classifiedItems.useful.remove(bestInv);  // don't have to consider this one any more
        }

        // finally, put any remaining items we haven't needed on the ground.
        for (ItemLoc i : classifiedItems.useful) {
            result.add(new ItemMovement(i.item, i.loc, Location.GROUND));
        }

        return result;
    }

    private static int getTotalBonus(DungeonItem item) {
        int total = 0;
        for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
            total += item.bonuses[i];
        }
        return total;
    }

    private static ItemLoc getBestOverallItem(List<ItemLoc> candidates) {
        ItemLoc bestItemLoc = null;
        int bestBonus = 0;
        for (ItemLoc candidate : candidates) {
            int totalBonus = getTotalBonus(candidate.item);
            if (bestItemLoc == null || totalBonus > bestBonus) {
                bestBonus = totalBonus;
                bestItemLoc = candidate;
            }
        }
        return bestItemLoc;
    }

    // takes the 'best' item that's better than a given item.
    private static ItemLoc getBestMajorizingItem(ItemLoc base, List<ItemLoc> candidates) {
        ItemLoc bestItemLoc = base;
        int bestBonus = getTotalBonus(base.item);
        for (ItemLoc candidate : candidates) {
            if (isBetter(candidate, base)) {
                int totalBonus = getTotalBonus(candidate.item);
                if (totalBonus > bestBonus) {
                    bestBonus = totalBonus;
                    bestItemLoc = candidate;
                }
            }
        }
        return bestItemLoc;
    }

    private static ItemLoc getEquipmentItem(List<ItemLoc> itemLocs) {
        for (ItemLoc itemLoc : itemLocs) {
            if (itemLoc.loc == Location.EQUIPMENT) {
                return itemLoc;
            }
        }
        return null;
    }

    // gets current inventory items, except any that might have been moved to equipment during optimization
    private static List<ItemLoc> getInventoryItems(ItemLoc equipment, List<ItemLoc> itemLocs) {
        List<ItemLoc> result = new ArrayList<>();
        for (ItemLoc itemLoc : itemLocs) {
            if (itemLoc.loc == Location.INVENTORY && !isBetterOrEqual(equipment, itemLoc)) {
                result.add(itemLoc);
            }
        }
        return result;
    }

    // gets items from the ground that duplicate items in the inventory
    private static List<ItemMovement> pickUpDuplicateItems(GameState gs) {
        List<ItemMovement> result = new ArrayList<>();
        Player player = gs.party.player;
        DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
        ItemList ground = square.items;
        ItemList inventory = player.inventory;

        for (DungeonItem item : ground.items) {
            // only apply for gold/potions/etc.
            if (item.slot != DungeonItem.Slot.NONE) {
                continue;
            }
            int numCanAdd = inventory.numCanAdd(item);
            boolean inInventory = inventory.findIdx(item) >= 0;
            if (item.flags.money || (inInventory && numCanAdd > 0)) {
                result.add(new ItemMovement(item, Location.GROUND, Location.INVENTORY));
            }
        }

        return result;
    }

    private static List<ItemMovement> pickUpImportantItems(GameState gs) {
        List<ItemMovement> result = new ArrayList<>();
        Player player = gs.party.player;
        DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
        ItemList ground = square.items;
        ItemList inventory = player.inventory;

        for (DungeonItem item : ground.items) {
            // pick up artifacts by default
            if (item.artifactSlot != DungeonItem.ArtifactSlot.NONE) {
                result.add(new ItemMovement(item, Location.GROUND, Location.INVENTORY));
                continue;
            }

            // pick up special items if possible
            int numCanAdd = inventory.numCanAdd(item);
            if (item.flags.special && (numCanAdd > 0)) {
                result.add(new ItemMovement(item, Location.GROUND, Location.INVENTORY));
            }
        }

        return result;
    }

    private static DungeonItem makeItem(String id, DungeonItem.Slot slot, int toHit, int toDam, int toDef) {
        return new DungeonItem(id, id, id, id, slot,
                DungeonItem.ArtifactSlot.NONE,
                new DungeonItem.Flags(false, false, false, false, false, false),
                new int[]{toHit, toDam, toDef, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                1, null);
    }

    public static void main(String[] args) {
        {
            System.out.println("test case 1:");
            DungeonItem sword1 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 0);
            DungeonItem sword2 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 1);
            DungeonItem sword3 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 1, 0);
            DungeonItem sword4 = makeItem("sword", DungeonItem.Slot.WEAPON, 1, 2, 0);
            DungeonItem sword5 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 0);
            DungeonItem sword6 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 2);

            ItemLoc[] itemLocs = {
                    new ItemLoc(sword1, Location.EQUIPMENT),
                    new ItemLoc(sword2, Location.INVENTORY),
                    new ItemLoc(sword3, Location.INVENTORY),
                    new ItemLoc(sword4, Location.INVENTORY),
                    new ItemLoc(sword5, Location.GROUND),
                    new ItemLoc(sword6, Location.GROUND)};

            List<ItemMovement> optimized = optimizeItems(Arrays.asList(itemLocs));
            for (ItemMovement entry: optimized) {
                System.out.println("raw: move " + entry.item.stringWithInfo() + " from " + entry.from + " to " + entry.to);
            }

            List<ItemMovement> simplified = simplifyMovements(optimized);
            for (ItemMovement entry: simplified) {
                System.out.println("final: move " + entry.item.stringWithInfo() + " from " + entry.from + " to " + entry.to);
            }

            // expected equipment: sword (+1, +2, +0)
            // expected inventory: sword (+0, +0, +2)
            // expected ground: everything else
        }

        {
            System.out.println("test case 2:");
            DungeonItem sword1 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 1);
            DungeonItem sword2 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 1);
            DungeonItem sword3 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 1, 0);
            DungeonItem sword4 = makeItem("sword", DungeonItem.Slot.WEAPON, 1, 2, 0);
            DungeonItem sword5 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 0);
            DungeonItem sword6 = makeItem("sword", DungeonItem.Slot.WEAPON, 0, 0, 2);

            ItemLoc[] itemLocs = {
                    new ItemLoc(sword1, Location.EQUIPMENT),
                    new ItemLoc(sword2, Location.INVENTORY),
                    new ItemLoc(sword3, Location.INVENTORY),
                    new ItemLoc(sword4, Location.INVENTORY),
                    new ItemLoc(sword5, Location.GROUND),
                    new ItemLoc(sword6, Location.GROUND)};

            List<ItemMovement> optimized = optimizeItems(Arrays.asList(itemLocs));
            for (ItemMovement entry: optimized) {
                System.out.println("move " + entry.item.stringWithInfo() + " from " + entry.from + " to " + entry.to);
            }

            List<ItemMovement> simplified = simplifyMovements(optimized);
            for (ItemMovement entry: simplified) {
                System.out.println("final: move " + entry.item.stringWithInfo() + " from " + entry.from + " to " + entry.to);
            }
            // expected equipment: sword (+0, +0, +2)
            // expected inventory: sword (+1, +2, +0)
            // expected ground: everything else
        }
    }
}
