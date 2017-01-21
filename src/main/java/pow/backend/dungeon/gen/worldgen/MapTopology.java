package pow.backend.dungeon.gen.worldgen;

import pow.backend.GameMap;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.mapgen.MapGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// TODO: this is design for future cleanup of dungeon/world generation
public class MapTopology {

    private static class Area {
        String id;  // also = name
        MapGenerator generator;
        List<MapConnection> connections;
    }

    private Map<String, Area> areas;

    public Map<String, GameMap> genWorld(Random rng) {
        Map<String, GameMap> world = new HashMap<>();
        for (Area area : this.areas.values()) {
            GameMap map = area.generator.genMap(area.id, area.connections, rng);
            world.put(area.id, map);
        }
        return world;
    }

    public void makeConnection( String area1id, MapConnection.Direction dir, String area2id) { }

    public void addAreas(MapTopology other) {
        // copy all areas from other to this
    }

    // thoughts on how this would work:
//    Areas= [
//            ("home town", townGen, 1)
//            ("happy fields", outsideMapGen[1], 2)
//            ("gladden fields", outsideMapGen[1], 2)
//            ("shiroku desert", outsideMapGen[2], 3)
//            ("deep desert", 3)
//            ("creepy forest", 4)
//            ("wild woolds", 4)
//            ("stormy sea", 5)
//            ];
//
//    MapGenerator[] outsideMapGen = ...
//    MapGenerator[] dungeonGen = ...
//
//    GenOverword go = new GenOverworld(areas);
//
//    GenDungeon gd1 = new GenDungeon("Dungeon 1", dungeonGen[1], 5);
//
//    MapTopology wt = go.getTopology();
//    wt.addAreas(gd1.getTopology());
//    wt.connect("happy fields", Down, "Dungeon1 Level 1");
}
