package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.utils.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.util.Array2D;
import pow.util.FractalMountain;
import pow.util.Point;

import java.util.List;
import java.util.Map;
import java.util.Random;

// makes a fractal mountain using recursive subdivision
public class MountainGenerator implements MapGenerator {

    public static class MapStyle {
        public final double[] breakpoints;
        public final TerrainFeatureTriplet[] terrainsAndFeatures;
        public final String upstairsFeatureId;
        public final String downstairsFeatureId;
        public final String openPortalFeatureId;
        public final String closedPortalFeatureId;

        public MapStyle(double[] breakpoints,
                        TerrainFeatureTriplet[] terrainsAndFeatures,
                        String upstairsFeatureId,
                        String downstairsFeatureId,
                        String openPortalFeatureId,
                        String closedPortalFeatureId) {
            this.breakpoints = breakpoints;
            this.terrainsAndFeatures = terrainsAndFeatures;
            this.upstairsFeatureId = upstairsFeatureId;
            this.downstairsFeatureId = downstairsFeatureId;
            this.openPortalFeatureId = openPortalFeatureId;
            this.closedPortalFeatureId = closedPortalFeatureId;
        }
    }

    private final int iterations;  // size of map will be approximately 2^iterations
    private final double roughness; // between 0 and 1; 1 is fully rough, 0 is perfectly smooth.
    private final MapStyle mapStyle;
    private final MonsterIdGroup monsterIds;
    private final int level;
    private final GameMap.Flags flags;

    public MountainGenerator(int iterations, MapStyle mapStyle, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        this.iterations = iterations;
        this.roughness = 0.5;
        this.mapStyle = mapStyle;
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
        double[] lowestBreakPointOnly = new double[]{mapStyle.breakpoints[0]};
        int[][] layers;
        do {
            double[][] elevations = fractalMountain.generateHeights(rng);
            int[][] wallsAndGround = discretizeElevations(elevations, lowestBreakPointOnly);
            isConnected = GeneratorUtils.hasConnectedRegionWithValue(wallsAndGround, 1);
            layers = discretizeElevations(elevations, mapStyle.breakpoints);
            // add border to ensure that there's a wall around the map.
            layers = addBorder(layers, 0);
        } while (!isConnected);

        TerrainFeatureTriplet[][] terrainMap = convertToTerrainAndFeatures(layers, mapStyle.terrainsAndFeatures);
        terrainMap = GeneratorUtils.trimTerrainBorder(terrainMap, mapStyle.terrainsAndFeatures[0].terrain);

        int w = Array2D.width(terrainMap);
        int h = Array2D.height(terrainMap);

        // generate fractal noise for feature placement
        double[][] noiseMap = GeneratorUtils.fractalNoise(w, h, 1.0, 8, 0.0, iterations);
        DungeonSquare[][] squares = GeneratorUtils.convertTerrainAndNoiseToDungeonSquares(terrainMap, noiseMap);

        // place the exits and get key locations
        GeneratorUtils.CommonIds commonIds = new GeneratorUtils.CommonIds(
                // note: assuming that index 0 corresponds to outer wall and that 1
                // is ground.
                mapStyle.terrainsAndFeatures[1].terrain,
                mapStyle.upstairsFeatureId,
                mapStyle.downstairsFeatureId,
                mapStyle.openPortalFeatureId,
                mapStyle.closedPortalFeatureId);
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                portalStatus,
                squares,
                commonIds,
                rng);

        // block the exits, if necessary
        //  if (style.addLockAroundExits) {
        //      GeneratorUtils.addLockAroundExits(squares, keyLocations, style.mainLock, style.surroundingLock);
        //  }

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

    private static double[][] makeNoise(int width, int height, int scale, int interpolationSteps) {
        return GeneratorUtils.fractalNoise(width, height, 1.0, scale, 0.0, interpolationSteps);
    }
}

