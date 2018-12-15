package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.worldgen.MapPoint;

import java.util.List;
import java.util.Random;

public interface MapGenerator {
    GameMap genMap(String id,
                   String name,
                   List<MapConnection> connections,
                   MapPoint.PortalStatus portalStatus,
                   Random rng);
}
