package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.gen.MapConnection;

import java.util.List;
import java.util.Random;

public interface MapGenerator {
    GameMap genMap(String name,
                   List<MapConnection> connections,
                   Random rng);

}
