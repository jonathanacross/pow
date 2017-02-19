package pow.backend.dungeon.gen;

import pow.backend.dungeon.DungeonExit;
import pow.util.Direction;

// TODO: rename to avoid name conflict with worldgen.SpacialConnection
public class MapConnection {
    // Location name in the starting map where the exit is;
    // will be added as a key location in the map so that
    // it's possible to return to this location.
    public String name;

    // Direction gives a hint while generating the dungeon where to put this
    // exit. E.g., if dir = S, then put the exit on the south side of the map.
    public Direction dir;

    // Area + location where this exit goes to.
    public DungeonExit destination;

    public MapConnection(String locName, Direction dir, String destAreaId, String destLocName) {
        this.name = locName;
        this.dir = dir;
        this.destination = new DungeonExit(destAreaId, destLocName);
    }
}
