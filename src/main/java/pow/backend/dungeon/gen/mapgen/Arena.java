package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.Constants;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.ProtoTranslator;
import pow.util.Point;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Arena implements MapGenerator {

    private ProtoTranslator translator;
    private List<String> monsterIds;
    private int width;
    private int height;
    private int level;

    public Arena(ProtoTranslator translator, List<String> monsterIds, int width, int height, int level) {
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.width = width;
        this.height = height;
        this.level = level;
    }

    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {

        int[][] data = genMap(this.width, this.height, rng);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, this.translator);

        // Add exits
        // TODO: this dungeon may have no guarantee that there's
        // a floor tile 1 away from the edge on each side, so
        // exit logic may fail.
        String upstairsFeatureId = translator.getFeature(Constants.FEATURE_UP_STAIRS).id;
        String downstairsFeatureId =  translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id;
        String floorTerrainId = translator.getTerrain(Constants.TERRAIN_FLOOR).id;
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                dungeonSquares,
                floorTerrainId,
                upstairsFeatureId,
                downstairsFeatureId,
                rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, this.monsterIds, null);
        return map;
    }

    private int[][] genMap(int width, int height, Random rng) {

        int[][] map = new int[width][height];

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                double x = c / (width - 1.0);
                double y = r / (height - 1.0);
                double d = Math.min(Math.min(x, y), Math.min(1.0 - x, 1.0 - y));
                double z = d - 0.5;
                double probWall = 16.0 * z * z * z * z;
                map[c][r] = (rng.nextDouble() < probWall) ?
                        Constants.TERRAIN_WALL :
                        Constants.TERRAIN_FLOOR;
            }
        }

        map[(int) (width * 0.25)][(int) (height * 0.3)] |= Constants.FEATURE_WIN_TILE;
        map[(int) (width * 0.75)][(int) (height * 0.6)] |= Constants.FEATURE_LOSE_TILE;

        return map;
    }
}
