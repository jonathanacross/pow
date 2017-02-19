package pow.backend.dungeon.gen.worldgen;


import pow.backend.dungeon.gen.mapgen.MapGenerator;
import pow.util.Direction;

import java.util.List;
import java.util.Set;

// This class holds the data necessary to create an overall spacial
// location of maps in the world (via the class MapTopology), and a
// map generator to create the map.
public class MapPoint {
    public String id;
    public int level;
    public int group;
    public List<Direction> fromDirs;
    public Set<Integer> fromGroups;
    public Set<String> fromIds;
    public MapGenerator mapGenerator;

    public MapPoint(String id,
                    int level,
                    int group,
                    List<Direction> fromDirs,
                    Set<Integer> fromGroups,
                    Set<String> fromIds,
                    MapGenerator mapGenerator) {
        this.id = id;
        this.level = level;
        this.group = group;
        this.fromDirs = fromDirs;
        this.fromGroups = fromGroups;
        this.fromIds = fromIds;
        this.mapGenerator = mapGenerator;
    }
}
