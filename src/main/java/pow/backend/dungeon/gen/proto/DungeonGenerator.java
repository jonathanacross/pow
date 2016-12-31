package pow.backend.dungeon.gen.proto;

import java.util.Random;

// TODO: need to separate out the difference between this and
// something that generates a Map.
public interface DungeonGenerator {
    int[][] genMap(int width, int height, Random randSeed);
}
