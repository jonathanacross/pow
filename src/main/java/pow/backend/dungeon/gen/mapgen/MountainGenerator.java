package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.FeatureData;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.TerrainData;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.util.Array2D;
import pow.util.FractalMountain;
import pow.util.Point;

import java.util.List;
import java.util.Map;
import java.util.Random;

// makes a fractal mountain using recursive subdivision
//public class MountainGenerator implements MapGenerator {
public class MountainGenerator implements MapGenerator {

    private final int iterations;  // size of map will be approximately 2^iterations
    private final double roughness; // between 0 and 1; 1 is fully rough, 0 is perfectly smooth.
    private final double[] breakpoints;
    private final TerrainFeatureTriplet[] terrainsAndFeatures;
    private final MonsterIdGroup monsterIds;
    private final int level;
    private final GameMap.Flags flags;

    public MountainGenerator(MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        // TODO: pull out these parameters
        this.iterations = 6;
        this.roughness = 0.5;
        this.breakpoints = new double[]{0.08, 0.16, 0.24, 0.32, 0.4, 0.5};

        this.terrainsAndFeatures = new TerrainFeatureTriplet[]{
                new TerrainFeatureTriplet("waves", null, null),
                new TerrainFeatureTriplet("water 4", null, null),
                new TerrainFeatureTriplet("water 3", null, null),
                new TerrainFeatureTriplet("water 2", null, null),
                new TerrainFeatureTriplet("water 1", null, null),
                new TerrainFeatureTriplet("sand", null, "palm tree"),
                new TerrainFeatureTriplet("grass", null, "palm tree")
        };
        this.monsterIds = monsterIds;
        this.level = level;
        this.flags = flags;
    }

    @Override
    public GameMap genMap(String name, List<MapConnection> connections, MapPoint.PortalStatus portalStatus, Random rng) {
        FractalMountain fractalMountain = new FractalMountain(iterations, roughness);

        // Generate an elevation map, and convert elevations into integer layers.
        // Make sure that the area of all layers except the lowest one is connected.
        boolean isConnected;
        double[] lowestBreakPointOnly = new double[]{breakpoints[0]};
        int[][] layers;
        do {
            double[][] elevations = fractalMountain.generateHeights(rng);
            int[][] wallsAndGround = discretizeElevations(elevations, lowestBreakPointOnly);
            isConnected = GeneratorUtils.hasConnectedRegionWithValue(wallsAndGround, 1);
            layers = discretizeElevations(elevations, breakpoints);
            // add border to ensure that there's a wall around the map.
            layers = addBorder(layers, 0);
        } while (!isConnected);

        TerrainFeatureTriplet[][] terrainMap = convertToTerrainAndFeatures(layers, terrainsAndFeatures);
        terrainMap = GeneratorUtils.trimTerrainBorder(terrainMap, terrainsAndFeatures[0].terrain);

        int w = Array2D.width(terrainMap);
        int h = Array2D.height(terrainMap);

        // generate fractal noise for feature placement
        // TODO: see good values for origWidth, origHeight
        double[][] noiseMap = makeNoise(w, h, 4, 4, iterations);

        // TODO: shared code with RecursiveInterpolation
        DungeonSquare[][] squares = new DungeonSquare[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                DungeonTerrain terrain = TerrainData.getTerrain(terrainMap[x][y].terrain);

                DungeonFeature feature = null;
                if (noiseMap[x][y] > 0.5 && terrainMap[x][y].feature1 != null) {
                    feature = FeatureData.getFeature(terrainMap[x][y].feature1);
                } else if (noiseMap[x][y] < -0.5 && terrainMap[x][y].feature2 != null) {
                    feature = FeatureData.getFeature(terrainMap[x][y].feature2);
                }

                squares[x][y] = new DungeonSquare(terrain, feature);
            }
        }


        // place the exits and get key locations
        // TODO: pass in these strings instead of hardcoding here.
        final String DUNGEON_ENTRANCE_ID = "dungeon entrance";
        final String TOWER_ENTRANCE_ID = "tower";
        final String OPEN_PORTAL_ID = "blue portal";
        final String CLOSED_PORTAL_ID = "gray portal";
        GeneratorUtils.CommonIds commonIds = new GeneratorUtils.CommonIds(
                terrainsAndFeatures[0].terrain,
                TOWER_ENTRANCE_ID, DUNGEON_ENTRANCE_ID, OPEN_PORTAL_ID, CLOSED_PORTAL_ID);
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                portalStatus,
                squares,
                commonIds,
                rng);

        // TODO: add locks around exits.
//        // block the exits, if necessary
//        if (style.addLockAroundExits) {
//            addLockAroundExits(squares, keyLocations, style.mainLock, style.surroundingLock);
//        }

        // add items
        int numItems = GeneratorUtils.getDefaultNumItems(w, h, rng);
        GeneratorUtils.addItems(level, squares, numItems, rng);

        return new GameMap(name, level, squares, keyLocations, new MonsterIdGroup(monsterIds), flags, null);
    }

    private static int[][] discretizeElevations(double[][] elevations, double[] breakPoints) {
        int width = Array2D.width(elevations);
        int height = Array2D.height(elevations);
        int[][] result = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = breakPoints.length; // set to last terrain if we don't find it.
                for (int i = 0; i < breakPoints.length; i++) {
                    if (elevations[x][y] <= breakPoints[i]) {
                        result[x][y] = i;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static int[][] addBorder(int[][] layers, int border) {
        int width = Array2D.width(layers) + 2;
        int height = Array2D.height(layers) + 2;
        int[][] result = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    result[x][y] = border;
                } else {
                    result[x][y] = layers[x - 1][y - 1];
                }
            }
        }
        return result;
    }

    private static TerrainFeatureTriplet[][] convertToTerrainAndFeatures(int[][] layers,
                                                                         TerrainFeatureTriplet[] terrainsAndFeatures) {
        int width = Array2D.width(layers);
        int height = Array2D.height(layers);
        TerrainFeatureTriplet[][] result = new TerrainFeatureTriplet[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = terrainsAndFeatures[layers[x][y]];
            }
        }
        return result;
    }

    // TODO: this is common code, but maybe should be simplified since origWidth, origheight make less sense.
    private static double[][] makeNoise(int width, int height, int origWidth, int origHeight, int interpolationSteps) {
        int scale = Math.max(origWidth, origHeight) * 2;
        return fractalNoise(width, height, 1.0, scale, 0.0, interpolationSteps);
    }

    // TODO: this is common code with RecursiveInterpolationGenerator.
    private static double[][] fractalNoise(int width, int height, double initAmp, double initScale, double delta, int iters) {
        double[][] data = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double t = 0.0;
                double amp = initAmp;
                double scale = initScale;

                for (int i = 0; i < iters; i++) {
                    t += amp * pow.util.SimplexNoise.noise(x / scale + delta, y / scale + delta);
                    amp *= 0.5;
                    scale *= 0.5;
                }

                data[x][y] = t;

            }
        }
        return data;
    }
}

