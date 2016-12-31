package pow.backend.dungeon.gen.proto;

import java.util.Random;

// This generates a prototype of a map.  The prototype encodes
// terrain/feature information as a single integer so that it's
// fast and easy to manipulate.
public interface ProtoGenerator {
    int[][] genMap(int width, int height, Random randSeed);
}
