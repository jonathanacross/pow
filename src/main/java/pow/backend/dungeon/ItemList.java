package pow.backend.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItemList implements Serializable {
    private final int maxItems;
    private int maxPerSlot;
    public final List<DungeonItem> items;

    public ItemList() { this(Integer.MAX_VALUE, Integer.MAX_VALUE); }

    public ItemList(int maxItems, int maxPerSlot) {
        this.maxItems = maxItems;
        this.maxPerSlot = maxPerSlot;
        items = new ArrayList<>();
    }

    public void increaseMaxPerSlot(int newMax) {
        // only allow increasing, as decreasing would require removing
        // items from the list.
        assert(newMax >= this.maxPerSlot);
        this.maxPerSlot = newMax;
    }

    public int size() {
        return items.size();
    }

    public DungeonItem get(int idx) { return items.get(idx); }

    public int numCanAdd(DungeonItem item) {
        int idx = findIdx(item);

        if (idx >= 0) {
            // already have this type of item
            int currNum = items.get(idx).count;
            return Math.min(item.count, maxPerSlot - currNum);
        } else {
            // a new item
            if (items.size() < maxItems) {
                return Math.min(item.count, maxPerSlot);
            } else {
                return 0;
            }
        }
    }

    public void add(DungeonItem item) {
        int idx = findIdx(item);

        if (idx >= 0) {
            items.get(idx).count += item.count;
        } else {
            // find where to put the new item
            int newLoc = 0;
            while (newLoc < items.size() && items.get(newLoc).compareTo(item) < 0) {
                newLoc++;
            }
            items.add(newLoc, item);
        }
    }

    public void removeOneItem(DungeonItem item) {
        int idx = findIdx(item);
        removeOneItemAt(idx);
    }

    public void removeOneItemAt(int idx) {
        int count = items.get(idx).count;
        if (count > 1) {
            items.get(idx).count -= 1;
        } else {
            items.remove(idx);
        }
    }

    // finds the index of an item
    // if not found, returns -1
    public int findIdx(DungeonItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(item))
                return i;
        }
        return -1;
    }
}

