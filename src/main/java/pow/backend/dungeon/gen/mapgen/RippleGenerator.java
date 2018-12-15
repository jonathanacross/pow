package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.Constants;
import pow.backend.dungeon.gen.FeatureData;
import pow.backend.dungeon.gen.MapConnection;
import pow.backend.dungeon.gen.TerrainData;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.backend.utils.GeneratorUtils;
import pow.util.Array2D;
import pow.util.Point;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class RippleGenerator implements MapGenerator {

    public static class MapStyle {
        public final String wallTerrainId;
        public final String floorTerrainId;
        public final String feature1Id;
        public final String feature2Id;
        public final String feature3Id;
        public final String upstairsFeatureId;
        public final String downstairsFeatureId;
        public final String openPortalFeatureId;
        public final String closedPortalFeatureId;

        public MapStyle(String wallTerrainId,
                        String floorTerrainId,
                        String feature1Id,
                        String feature2Id,
                        String feature3Id,
                        String upstairsFeatureId,
                        String downstairsFeatureId,
                        String openPortalFeatureId,
                        String closedPortalFeatureId) {
            this.wallTerrainId = wallTerrainId;
            this.floorTerrainId = floorTerrainId;
            this.feature1Id = feature1Id;
            this.feature2Id = feature2Id;
            this.feature3Id = feature3Id;
            this.upstairsFeatureId = upstairsFeatureId;
            this.downstairsFeatureId = downstairsFeatureId;
            this.openPortalFeatureId = openPortalFeatureId;
            this.closedPortalFeatureId = closedPortalFeatureId;
        }
    }

    private final int size;
    private final MapStyle mapStyle;
    private final MonsterIdGroup monsterIds;
    private final int level;
    private final GameMap.Flags flags;

    public RippleGenerator(int size,
                    MapStyle mapStyle, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        this.mapStyle = mapStyle;
        this.monsterIds = monsterIds;
        this.level = level;
        this.flags = flags;
        this.size = size;
    }

    @Override
    public GameMap genMap(String id, String name,
                          List<MapConnection> connections, MapPoint.PortalStatus portalStatus, Random rng) {
        int[][] terrain = drawEdges(rng);
        int[][] features = drawRipple(rng);

        TerrainFeatureTriplet[][] terrainMap = convertToTerrainAndFeatures(terrain, features);
        terrainMap = GeneratorUtils.trimTerrainBorder(terrainMap, mapStyle.wallTerrainId);

        DungeonSquare[][] squares = convertTerrainToDungeonSquares(terrainMap);

        // place the exits and get key locations
        GeneratorUtils.CommonIds commonIds = new GeneratorUtils.CommonIds(
                mapStyle.floorTerrainId,
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
//      if (mapStyle.addLockAroundExits) {
//          GeneratorUtils.addLockAroundExits(squares, keyLocations, mapStyle.mainLock, mapStyle.surroundingLock);
//      }

        // add items
        int numItems = GeneratorUtils.getDefaultNumItems(size, size, rng);
        GeneratorUtils.addItems(level, squares, numItems, rng);

        return new GameMap(id, name, level, squares, keyLocations, new MonsterIdGroup(monsterIds), flags, null);
    }

    private TerrainFeatureTriplet[][] convertToTerrainAndFeatures(int[][] terrain, int[][] features) {
        TerrainFeatureTriplet[][] result = new TerrainFeatureTriplet[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                boolean isFloor = terrain[x][y] == Constants.TERRAIN_FLOOR;
                String terrainStr = isFloor ? mapStyle.floorTerrainId : mapStyle.wallTerrainId;
                String featureStr = null;
                if (isFloor && features[x][y] == 1) {
                    featureStr = mapStyle.feature1Id;
                }
                if (isFloor && features[x][y] == 2) {
                    featureStr = mapStyle.feature2Id;
                }
                if (isFloor && features[x][y] == 3) {
                    featureStr = mapStyle.feature3Id;
                }
                result[x][y] = new TerrainFeatureTriplet(terrainStr, featureStr, null);
            }
        }
        return result;
    }

    private static DungeonSquare[][] convertTerrainToDungeonSquares(TerrainFeatureTriplet[][] terrainMap) {
        int w = Array2D.width(terrainMap);
        int h = Array2D.height(terrainMap);

        DungeonSquare[][] squares = new DungeonSquare[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                DungeonTerrain terrain = TerrainData.getTerrain(terrainMap[x][y].terrain);
                DungeonFeature feature = null;
                if (terrainMap[x][y].feature1 != null) {
                    feature = FeatureData.getFeature(terrainMap[x][y].feature1);
                }
                squares[x][y] = new DungeonSquare(terrain, feature);
            }
        }

        return squares;
    }

    private void drawCircle(int[][] squares, int x0, int y0, int r, int eraseColor, int fillColor, int fillPercent, Random rng) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if ((x - x0) * (x - x0) + (y - y0) * (y - y0) <= r * r) {
                    if (fillColor == eraseColor || rng.nextInt(100) < fillPercent) {
                        squares[x][y] = fillColor;
                    }
                }
            }
        }
    }

    private void drawTarget(int[][] squares, int x0, int y0, int rMax, int eraseColor, int fillColor, int fillPercent, Random rng) {
        for (int r = rMax; r > 0; r -= 3) {
            drawCircle(squares, x0, y0, r, eraseColor, fillColor, fillPercent, rng);
            drawCircle(squares, x0, y0, r - 1, eraseColor, eraseColor, fillPercent, rng);
        }
    }

    private void drawOtherFeatures(int[][] squares, Random rng) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (squares[x][y] == 1) {
                    continue;
                }

                double r = rng.nextDouble();
                if (r < 0.09) {
                    squares[x][y] = 2;
                } else if (r < 0.15) {
                    squares[x][y] = 3;
                }
            }
        }
    }

    private int[][] drawRipple(Random rng) {
        int[][] squares = new int[size][size];

        for (int i = 0; i < 200; i++) {
            int r = rng.nextInt(size / 4) + size / 8;
            int x0 = rng.nextInt(size + size / 2) - size / 4;
            int y0 = rng.nextInt(size + size / 2) - size / 4;
            drawTarget(squares, x0, y0, r, 0, 1, 60, rng);
        }

        drawOtherFeatures(squares, rng);

        return squares;
    }

    // Defines ground/wall regions.  Note that there is no guarantee that the
    // ground will be connected.
    private int[][] tryDrawEdges(int size, Random rng) {
        int[][] squares = GeneratorUtils.solidMap(size, size);

        // Carve out the interior.
        for (int i = 0; i < 50; i++) {
            int x0 = rng.nextInt(size * 3 / 4) + (size / 8);
            int y0 = rng.nextInt(size * 3 / 4) + (size / 8);
            drawCircle(squares, x0, y0, 5, Constants.TERRAIN_FLOOR, Constants.TERRAIN_FLOOR, 100, rng);
        }

        // Draw a border around the outside just to make sure there is a wall.
        for (int i = 0; i < size; i++) {
            squares[i][0] = Constants.TERRAIN_WALL;
            squares[i][size - 1] = Constants.TERRAIN_WALL;
        }
        for (int i = 0; i < size; i++) {
            squares[0][i] = Constants.TERRAIN_WALL;
            squares[size - 1][i] = Constants.TERRAIN_WALL;
        }

        return squares;
    }

    private int[][] drawEdges(Random rng) {
        int[][] squares;
        do {
            squares = tryDrawEdges(size, rng);
        } while (!GeneratorUtils.hasConnectedRegionWithValue(squares, Constants.TERRAIN_FLOOR));
        return squares;
    }
}
