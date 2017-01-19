package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;

import java.util.Map;
import java.util.Random;

public interface MapGenerator {
    GameMap genMap(String name,
                   Map<String, String> exits,  // name of this exit -> otherAreaId@otherAreaLocName
                   Random rng);

}
