package pow.backend.dungeon.gen;

import pow.backend.dungeon.DungeonExit;
import pow.util.Direction;

public class MapConnection {
    // Location name in the starting map where the exit is;
    // will be added as a key location in the map so that
    // it's possible to return to this location.
    public final String name;

    // Direction gives a hint while generating the dungeon where to put this
    // exit. E.g., if dir = S, then put the exit on the south side of the map.
    public final Direction dir;

    // Area + location where this exit goes to.
    public final DungeonExit destination;

    public MapConnection(String locName, Direction dir, String destAreaId, String destLocName) {
        this.name = locName;
        this.dir = dir;
        this.destination = new DungeonExit(destAreaId, destLocName);
    }
}
