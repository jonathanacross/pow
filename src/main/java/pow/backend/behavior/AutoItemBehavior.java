package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.*;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.ItemList;

import java.util.*;

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
public class AutoItemBehavior implements Behavior {

    private List<Action> plannedActions;

    public AutoItemBehavior(GameState gs) {
        this.plannedActions = optimizeItems(gs);
    }

    @Override
    public boolean canPerform(GameState gameState) {
        return !plannedActions.isEmpty();
    }

    @Override
    public Action getAction() {
        Action a = plannedActions.remove(0);
        return a;
    }

    // --------------------- private implementation ------------

    enum Location {
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

    private static class ItemLoc {
        DungeonItem item;
        Location loc;

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

    private static Map<ItemLoc, Location> optimizeItems(List<ItemLoc> itemLocs) {
        // handle some degenerate, simple cases
        Map<ItemLoc, Location> result = new HashMap<>();
        if (itemLocs.isEmpty()) {
            return result;
        }

        if (itemLocs.size() == 1) {
            result.put(itemLocs.get(0), Location.EQUIPMENT);
            return result;
        }

        // now the real work.

        // first step: remove items that are clearly bad.
        UsefulAndUselessItems classifiedItems = classifyItems(itemLocs);
        for (ItemLoc i : classifiedItems.useless) {
            result.put(i, Location.GROUND);
        }

        // if equipment is empty, then pick the overall best one for equipment
        ItemLoc equipment = getEquipmentItem(itemLocs);
        if (equipment == null) {
            equipment = getBestOverallItem(classifiedItems.useful);
        }
        ItemLoc bestEquipment = getBestMajorizingItem(equipment, classifiedItems.useful);
        result.put(bestEquipment, Location.EQUIPMENT);
        classifiedItems.useful.remove(bestEquipment);  // don't have to consider this one any more

        // try to find majorizing items for remaining inventory items
        List<ItemLoc> currentInventory = getInventoryItems(bestEquipment, itemLocs);
        for (ItemLoc invItem : currentInventory) {
            ItemLoc bestInv = getBestMajorizingItem(invItem, classifiedItems.useful);
            result.put(bestInv, Location.INVENTORY);
            classifiedItems.useful.remove(bestInv);  // don't have to consider this one any more
        }

        // finally, put any remaining items we haven't needed on the ground.
        for (ItemLoc i : classifiedItems.useful) {
            result.put(i, i.loc);
        }

        return result;
    }

    private static List<Action> makeActions(Map<ItemLoc, Location> classifiedItems, GameState gs) {
        List<Action> actions = new ArrayList<>();
        Player player = gs.party.player;
        DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
        ItemList groundItems = square.items;
        ItemList inventoryItems = player.inventory;

        // do drop actions first
        for (Map.Entry<ItemLoc, Location> entry : classifiedItems.entrySet()) {
            DungeonItem item = entry.getKey().item;
            Location oldLoc = entry.getKey().loc;
            Location newLoc = entry.getValue();
            if (oldLoc != newLoc && newLoc == Location.GROUND) {
                if (oldLoc == Location.INVENTORY) {
                    int itemLoc = player.inventory.items.indexOf(item);
                    if (itemLoc >= 0) {
                        actions.add(new Drop(player, itemLoc, item.count));
                    } else {
                        System.out.println("error: couldn't find item " + item.stringWithInfo() + " in player's inventory");
                    }
                } else if (oldLoc == Location.EQUIPMENT) {
                    int itemLoc = player.equipment.items.indexOf(item);
                    if (itemLoc >= 0) {
                        actions.add(new DropEquipment(player, itemLoc));
                    } else {
                        System.out.println("error: couldn't find item " + item.stringWithInfo() + " in player's equipment");
                    }
                }
                System.out.println("move " + item.stringWithInfo() + " from " + oldLoc + " to " + newLoc);
            }
        }

        // next do equip actions
        for (Map.Entry<ItemLoc, Location> entry : classifiedItems.entrySet()) {
            DungeonItem item = entry.getKey().item;
            Location oldLoc = entry.getKey().loc;
            Location newLoc = entry.getValue();
            if (oldLoc != newLoc && newLoc == Location.EQUIPMENT) {
                ItemList sourceList = oldLoc == Location.INVENTORY ? inventoryItems : groundItems;
                int itemLoc = sourceList.items.indexOf(item);
                if (itemLoc >= 0) {
                    actions.add(new Wear(player, sourceList, itemLoc));
                } else {
                    System.out.println("error: couldn't find item " + item.stringWithInfo());
                }
            }
        }

        // finally do get actions
        for (Map.Entry<ItemLoc, Location> entry : classifiedItems.entrySet()) {
            DungeonItem item = entry.getKey().item;
            Location oldLoc = entry.getKey().loc;
            Location newLoc = entry.getValue();
            if (oldLoc == Location.GROUND && newLoc == Location.INVENTORY) {
                int itemLoc = groundItems.items.indexOf(item);
                if (itemLoc >= 0) {
                    actions.add(new PickUp(player, itemLoc, item.count));
                } else {
                    System.out.println("error: couldn't find item " + item.stringWithInfo() + " in player's inventory");
                }
            }
        }

        return actions;
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
                if (bestItemLoc == null || totalBonus > bestBonus) {
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
    private static List<Action> pickUpDuplicateItems(GameState gs) {
        List<Action> actions = new ArrayList<>();
        Player player = gs.party.player;
        DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
        ItemList ground = square.items;
        ItemList inventory = player.inventory;

        for (DungeonItem item : ground.items) {
            // only apply for gold/potions/etc.
            if (item.slot != DungeonItem.Slot.NONE) {
                continue;
            }
            int groundIndex = ground.findIdx(item);
            int numCanAdd = inventory.numCanAdd(item);
            boolean inInventory = inventory.findIdx(item) >= 0;
            if (item.flags.money || (inInventory && numCanAdd > 0)) {
                actions.add(new PickUp(player, groundIndex, numCanAdd));
            }
        }

        return actions;
    }

    private static List<Action> optimizeItems(GameState gs) {
        Player player = gs.party.player;
        DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
        List<DungeonItem> ground = square.items.items;
        List<DungeonItem> inventory = player.inventory.items;
        List<DungeonItem> equipment = player.equipment.items;

        List<Action> result = new ArrayList<>();
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

            Map<ItemLoc, Location> sortedItems = optimizeItems(itemLocs);
            result.addAll(makeActions(sortedItems, gs));
        }

        result.addAll(pickUpDuplicateItems(gs));

        return result;
    }

    private static DungeonItem makeItem(String id, DungeonItem.Slot slot, int toHit, int toDam, int toDef) {
        return new DungeonItem(id, id, id, id, slot,
                DungeonItem.ArtifactSlot.NONE,
                new DungeonItem.Flags(false, false, false, false),
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

            Map<ItemLoc, Location> optimized = optimizeItems(Arrays.asList(itemLocs));
            for (Map.Entry<ItemLoc, Location> entry : optimized.entrySet()) {
                DungeonItem item = entry.getKey().item;
                Location oldLoc = entry.getKey().loc;
                Location newLoc = entry.getValue();
                System.out.println("move " + item.stringWithInfo() + " from " + oldLoc + " to " + newLoc);
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

            Map<ItemLoc, Location> optimized = optimizeItems(Arrays.asList(itemLocs));
            for (Map.Entry<ItemLoc, Location> entry : optimized.entrySet()) {
                DungeonItem item = entry.getKey().item;
                Location oldLoc = entry.getKey().loc;
                Location newLoc = entry.getValue();
                System.out.println("move " + item.stringWithInfo() + " from " + oldLoc + " to " + newLoc);
            }
            // expected equipment: sword (+0, +0, +2)
            // expected inventory: sword (+1, +2, +0)
            // expected ground: everything else
        }
    }
}
