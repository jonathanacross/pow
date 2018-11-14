package pow.backend.dungeon;

import pow.backend.GameConstants;
import pow.backend.GameMap;

import java.io.Serializable;
import java.util.LinkedList;

// Stack to hold recent maps.
// When the player goes to an area that isn't in most recent k levels,
// then monsters need to be regenerated.
public class RecentMaps implements Serializable {
    private final LinkedList<GameMap> recentLocations;

    public RecentMaps() {
        recentLocations = new LinkedList<>();
    }

    // pushes the new map on the stack.
    // returns true if monsters need to be regenerated for this map
    public boolean setMap(GameMap map) {

        // if we can find the map in the top first locations, don't need to regen
        int numToCheck = Math.min(GameConstants.NUM_RECENT_LOCS_BEFORE_REGEN, recentLocations.size());
        boolean needsRegen = true;
        for (int i = 0; i < numToCheck; i++) {
            if (recentLocations.get(i) == map) {
                needsRegen = false;
            }
        }

        // update the stack
        recentLocations.remove(map);
        recentLocations.addFirst(map);

        return needsRegen;
    }

    public GameMap getCurrentMap() {
        return recentLocations.get(0);
    }
}
