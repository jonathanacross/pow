package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.utils.GeneratorUtils;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.util.Array2D;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RecursiveInterpolation implements MapGenerator {

    // expand/modify this class to make richer areas
    public static class MapStyle {
        public final TerrainFeatureTriplet border;
        public final TerrainFeatureTriplet interior;
        public final String upstairsFeatureId;
        public final String downstairsFeatureId;
        public final String openPortalFeatureId;
        public final String closedPortalFeatureId;
        public final boolean addLockAroundExits;
        public final TerrainFeatureTriplet surroundingLock;
        public final TerrainFeatureTriplet mainLock;

        public MapStyle(TerrainFeatureTriplet border,
                        TerrainFeatureTriplet interior,
                        String upstairsFeatureId,
                        String downstairsFeatureId,
                        String openPortalFeatureId,
                        String closedPortalFeatureId,
                        boolean addLockAroundExits,
                        TerrainFeatureTriplet surroundingLock,
                        TerrainFeatureTriplet mainLock) {
            this.border = border;
            this.interior = interior;
            this.upstairsFeatureId = upstairsFeatureId;
            this.downstairsFeatureId = downstairsFeatureId;
            this.openPortalFeatureId = openPortalFeatureId;
            this.closedPortalFeatureId = closedPortalFeatureId;
            this.addLockAroundExits = addLockAroundExits;
            this.surroundingLock = surroundingLock;
            this.mainLock = mainLock;
        }
    }

    private final int sourceSize;
    private final int numInterpolationSteps;
    private final MapStyle mapStyle;
    private final int level;
    private final MonsterIdGroup monsterIds;
    private final GameMap.Flags flags;

    public RecursiveInterpolation(int sourceSize,
                                  int numInterpolationSteps,
                                  MapStyle mapStyle,
                                  MonsterIdGroup monsterIds,
                                  int level,
                                  GameMap.Flags flags) {
        this.sourceSize = sourceSize;
        this.numInterpolationSteps = numInterpolationSteps;
        this.mapStyle = mapStyle;
        this.monsterIds = monsterIds;
        this.level = level;
        this.flags = flags;
    }

    @Override
    public GameMap genMap(String id, String name, List<MapConnection> connections,
                          MapPoint.PortalStatus portalStatus, Random rng) {
        return genMap(id, name, level, sourceSize, sourceSize, numInterpolationSteps, mapStyle, monsterIds,
                connections, portalStatus, flags, rng);
    }

    private static GameMap genMap(
            String id,
            String name,
            int level,
            int width,
            int height,
            int numInterpolationSteps,
            MapStyle style,
            MonsterIdGroup monsterIds,
            List<MapConnection> connections,
            MapPoint.PortalStatus portalStatus,
            GameMap.Flags flags,
            Random rng) {

        // build the terrain
        TerrainFeatureTriplet[][] layout = genTerrainLayout(width, height, style, rng);
        TerrainFeatureTriplet[][] terrainMap = makeInterpMap(layout, rng, numInterpolationSteps);
        terrainMap = GeneratorUtils.trimTerrainBorder(terrainMap, style.border.terrain);
        int w = Array2D.width(terrainMap);
        int h = Array2D.height(terrainMap);

        // generate fractal noise for feature placement
        double[][] noiseMap = makeNoise(w, h, width, height, numInterpolationSteps);

        DungeonSquare[][] squares = GeneratorUtils.convertTerrainAndNoiseToDungeonSquares(terrainMap, noiseMap);

        // place the exits and get key locations
        GeneratorUtils.CommonIds commonIds = new GeneratorUtils.CommonIds(
                style.interior.terrain,
                style.upstairsFeatureId,
                style.downstairsFeatureId,
                style.openPortalFeatureId,
                style.closedPortalFeatureId);
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                portalStatus,
                squares,
                commonIds,
                rng);

        // block the exits, if necessary
        if (style.addLockAroundExits) {
            GeneratorUtils.addLockAroundExits(squares, keyLocations, style.mainLock, style.surroundingLock);
        }

        // add items
        int numItems = GeneratorUtils.getDefaultNumItems(w, h, rng);
        GeneratorUtils.addItems(level, squares, numItems, rng);

        return new GameMap(id, name, level, squares, keyLocations, new MonsterIdGroup(monsterIds), flags,null);
    }

    // fills in squares such that the open squares are connected
    private static List<Point> findSafeInternalSquaresToBlock(int width, int height, int desiredBlocked, Random rng) {
        int[][] blocked = new int[width][height];

        // make the edges blocked initially
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocked[x][y] = ((x == 0) || (x == width - 1) || (y == 0) || (y == height - 1)) ? 1 : 0;
            }
        }

        // goal is to block squares in the center, so that the unblocked
        // part is still connected.
        List<Point> safeBlocks = new ArrayList<>();
        for (int i = 0; i < desiredBlocked; ) {
            int x;
            int y;
            // find a random empty square
            do {
                x = rng.nextInt(width - 2) + 1;
                y = rng.nextInt(height - 2) + 1;
            } while (blocked[x][y] == 1);
            blocked[x][y] = 1;
            if (GeneratorUtils.hasConnectedRegionWithValue(blocked, 0)) {
                // was good, keep it
                safeBlocks.add(new Point(x,y));
                i++;
            } else {
                // was a bad one, so remove it
                blocked[x][y] = 0;
            }
        }

        return safeBlocks;
    }

    // removes the features at random from the source triplet
    private static TerrainFeatureTriplet mixup(TerrainFeatureTriplet source, Random rng) {
        String terrain = source.terrain;
        String feature1 = rng.nextBoolean() ? source.feature1 : null;
        String feature2 = rng.nextBoolean() ? source.feature2 : null;
        return new TerrainFeatureTriplet(terrain, feature1, feature2);
    }

    private static TerrainFeatureTriplet[][] genTerrainLayout(
            int width, int height, MapStyle style, Random rng) {

        TerrainFeatureTriplet[][] layout = new TerrainFeatureTriplet[width][height];

        // fill a border around the edge
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean isEdge = ((x == 0) || (x == width - 1) || (y == 0) || (y == height - 1));
                if (isEdge) {
                    // for now, just using first border; later extend this to multiple types.
                    layout[x][y] = mixup(style.border, rng);
                } else {
                    layout[x][y] = mixup(style.interior, rng);
                }
            }
        }

        // Add squares of impassible in the middle (for dungeon shape variety)
        int x = rng.nextInt(19) + rng.nextInt(19);  // pick number from 0 to 36, 18 is most likely
        int numPossibleSquares = (width - 2)*(height - 2);
        int numDesiredToFill = (int) Math.round(x / 100.0 * numPossibleSquares);
        List<Point> safeBlocks = findSafeInternalSquaresToBlock(width, height, numDesiredToFill, rng);
        for (Point p : safeBlocks) {
            layout[p.x][p.y] = mixup(style.border, rng);
        }

        return layout;
    }

    private static TerrainFeatureTriplet[][] interpolate(TerrainFeatureTriplet[][] layout, Random rng) {

        int width = Array2D.width(layout);
        int height = Array2D.width(layout);
        TerrainFeatureTriplet[][] interpMap = new TerrainFeatureTriplet[2 * width][2 * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // non-interpolated squares
                interpMap[2 * x][2 * y] = layout[x][y];

                int xn = (x + 1) % width;
                int yn = (y + 1) % height;

                // interpolate first order
                if (rng.nextInt(2) == 0) {
                    interpMap[2 * x + 1][2 * y] = layout[x][y];
                } else {
                    interpMap[2 * x + 1][2 * y] = layout[xn][y];
                }
                if (rng.nextInt(2) == 0) {
                    interpMap[2 * x][2 * y + 1] = layout[x][y];
                } else {
                    interpMap[2 * x][2 * y + 1] = layout[x][yn];
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // second order interpolation (corners)
                int xxn = (2 * x + 2) % Array2D.width(interpMap);
                int yyn = (2 * y + 2) % Array2D.height(interpMap);
                switch (rng.nextInt(4)) {
                    case 0:
                        interpMap[2 * x + 1][2 * y + 1] = interpMap[2 * x + 1][2 * y];
                        break;
                    case 1:
                        interpMap[2 * x + 1][2 * y + 1] = interpMap[2 * x][2 * y + 1];
                        break;
                    case 2:
                        interpMap[2 * x + 1][2 * y + 1] = interpMap[xxn][2 * y + 1];
                        break;
                    case 3:
                        interpMap[2 * x + 1][2 * y + 1] = interpMap[2 * x + 1][yyn];
                        break;
                }
            }
        }
//        printMap(interpMap);

        return interpMap;
    }

    private static void printMap(TerrainFeatureTriplet[][] map) {
        for (int y = 0; y < Array2D.height(map); y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < Array2D.width(map); x++) {
                line.append(map[x][y]);
                line.append(' ');
            }
            System.out.println(line.toString());
        }
        System.out.println();
    }

    private static TerrainFeatureTriplet[][] makeInterpMap(TerrainFeatureTriplet[][] layout, Random rng, int interpolationSteps) {
        TerrainFeatureTriplet[][] map = layout;
        for (int i = 0; i < interpolationSteps; i++) {
            map = interpolate(map, rng);
        }
        return map;
    }

    private static double[][] makeNoise(int width, int height, int origWidth, int origHeight, int interpolationSteps) {
        int scale = Math.max(origWidth, origHeight) * 2;
        return GeneratorUtils.fractalNoise(width, height, 1.0, scale, 0.0, interpolationSteps);
    }
}
