package pow.backend;

import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;

import java.io.Serializable;
import java.util.Random;

public class GameMap implements Serializable {
    public DungeonSquare[][] map; // indexed by x,y, or c,r
    public int width;
    public int height;

    private DungeonSquare[][] buildArena(int width, int height, int seed) {
        Random rng = new Random(seed);
        char[][] map = new char[width][height];

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                double x = c / (width - 1.0);
                double y = r / (height - 1.0);
                double d = Math.min(Math.min(x, y), Math.min(1.0 - x, 1.0 - y));
                double z = d - 0.5;
                double probWall = 16.0 * z * z * z * z;
                map[c][r] = (rng.nextDouble() < probWall) ? '#' : '.';
            }
        }

        DungeonSquare[][] dungeonMap = new DungeonSquare[width][height];
        // TODO: remove image from backend?
        DungeonTerrain wall = new DungeonTerrain("big stone wall", "big stone wall",
                new DungeonTerrain.Flags(true));
        DungeonTerrain floor = new DungeonTerrain("floor", "floor",
                new DungeonTerrain.Flags(false));

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                dungeonMap[c][r] = map[c][r] == '#' ?
                        new DungeonSquare(wall, null) :
                        new DungeonSquare(floor, null);
            }
        }

        // add win/lose features
        dungeonMap[(int) (width * 0.25)][(int) (height * 0.3)].feature =
                new DungeonFeature("orange pearl", "orange pearl",
                        new DungeonFeature.Flags(false));
        dungeonMap[(int) (width * 0.75)][(int) (height * 0.6)].feature =
                new DungeonFeature("cobra", "cobra",
                        new DungeonFeature.Flags(false));

        return dungeonMap;
    }


    public GameMap() {
        width = 40;
        height = 30;
        map = buildArena(width, height, 123);
    }
}