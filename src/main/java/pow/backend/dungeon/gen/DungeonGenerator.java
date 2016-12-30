package pow.backend.dungeon.gen;

import java.util.Random;

public interface DungeonGenerator {
    int[][] genMap(int width, int height, Random randSeed);
}
