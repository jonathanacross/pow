package pow.backend.dungeon.gen.worldgen;

import java.io.IOException;
import java.util.*;

public class GenTopoTest {

    public static void main(String[] args) throws IOException {
        List<MapGenData> data = MapGenData.readLinkData();
        Random rng = new Random();
        MapTopology topology = new MapTopology(data, rng, 0.25);
    }
}
