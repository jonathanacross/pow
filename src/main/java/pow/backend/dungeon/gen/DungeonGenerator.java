package pow.backend.dungeon.gen;

import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.dungeon.gen.proto.GenUtils;
import pow.backend.dungeon.gen.proto.ProtoGenerator;
import pow.util.Array2D;
import pow.util.DebugLogger;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonGenerator {
    public static DungeonSquare[][] generateMap(ProtoGenerator generator, ProtoTranslator translator, int width, int height, Random rng) {

        int[][] squares = generator.genMap(width, height, rng);
        DebugLogger.info(GenUtils.getMapString(squares));

        DungeonSquare[][] dungeonMap = new DungeonSquare[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                DungeonTerrain terrain = translator.getTerrain(squares[x][y]);
                DungeonFeature feature = translator.getFeature(squares[x][y]);
                dungeonMap[x][y] = new DungeonSquare(terrain, feature);
            }
        }

        return dungeonMap;
    }

    public static List<Actor> createMonsters(DungeonSquare[][] dungeonMap, int numMonsters, Random rng) {
        List<Actor> actors = new ArrayList<>();
        int width = Array2D.width(dungeonMap);
        int height = Array2D.height(dungeonMap);
        for (int i = 0; i < numMonsters; i++) {
            int x;
            int y;
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (dungeonMap[x][y].blockGround());
            Point location = new Point(x,y);
            String id = "";
            switch (i % 7) {
                case 0: id = "ant"; break;
                case 1: id = "yellow mushroom patch"; break;
                case 2: id = "white rat"; break;
                case 3: id = "bat"; break;
                case 4: id = "yellow snake"; break;
                case 5: id = "floating eye"; break;
                case 6: id = "chess knight"; break;
            }
            actors.add(MonsterGenerator.genMonster(id, rng, location));
        }
        return actors;
    }
}
