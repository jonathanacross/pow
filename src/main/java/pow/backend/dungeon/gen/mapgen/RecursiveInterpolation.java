package pow.backend.dungeon.gen.mapgen;

import pow.backend.ActionParams;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.*;
import pow.backend.dungeon.gen.*;
import pow.util.Array2D;
import pow.util.DieRoll;
import pow.util.Point;

import java.util.*;

public class RecursiveInterpolation implements MapGenerator {

    public static class TerrainFeatureTriplet {
        public String terrain;
        public String feature1;
        public String feature2;

        public TerrainFeatureTriplet(String terrain, String feature1, String feature2) {
            this.terrain = terrain;
            this.feature1 = feature1;
            this.feature2 = feature2;
        }

        @Override
        // just a rough thing for debugging
        public String toString() {
            char t = terrain == null ? '_' : terrain.charAt(0);
            char f1 = feature1 == null ? '_' : feature1.charAt(0);
            char f2 = feature2 == null ? '_' : feature2.charAt(0);
            return "" + t + f1 + f2;
        }
    }

    // expand/modify this class to make richer areas
    public static class MapStyle {
        public List<TerrainFeatureTriplet> borders;
        public List<TerrainFeatureTriplet> interiors;
        public List<String> monsterIds;
        public String upstairsFeatureId;
        public String downstairsFeatureId;

        public MapStyle(List<TerrainFeatureTriplet> borders,
                        List<TerrainFeatureTriplet> interiors,
                        List<String> monsterIds,
                        String upstairsFeatureId,
                        String downstairsFeatureId) {
            this.borders = borders;
            this.interiors = interiors;
            this.monsterIds = monsterIds;
            this.upstairsFeatureId = upstairsFeatureId;
            this.downstairsFeatureId = downstairsFeatureId;
        }
    }

    private int sourceSize;
    private int numInterpolationSteps;
    private MapStyle mapStyle;
    public RecursiveInterpolation(int sourceSize, int numInterpolationSteps, MapStyle mapStyle) {
        this.sourceSize = sourceSize;
        this.numInterpolationSteps = numInterpolationSteps;
        this.mapStyle = mapStyle;
    }

    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {
        return genMap(name, sourceSize, sourceSize, numInterpolationSteps, mapStyle, connections, rng);
    }

    private static GameMap genMap(
            String name,
            int width,
            int height,
            int numInterpolationSteps,
            MapStyle style,
            List<MapConnection> connections,
            Random rng) {

        // get the border types; used in various functions below
        Set<String> borders = new HashSet<>();
        for (TerrainFeatureTriplet b : style.borders) {
            borders.add(b.terrain);
        }

        // build the terrain
        TerrainFeatureTriplet[][] layout = genTerrainLayout(width, height, style, rng);
        TerrainFeatureTriplet[][] terrainMap = makeInterpMap(layout, rng, numInterpolationSteps);
        terrainMap = trimTerrainBorder(terrainMap, borders);
        int w = Array2D.width(terrainMap);
        int h = Array2D.height(terrainMap);

        // generate fractal noise for feature placement
        double[][] noiseMap = makeNoise(w, h, width, height, numInterpolationSteps);

        DungeonSquare[][] squares = new DungeonSquare[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                DungeonTerrain terrain = TerrainData.getTerrain(terrainMap[x][y].terrain);

                DungeonFeature feature = null;
                if (noiseMap[x][y] > 0.5 && terrainMap[x][y].feature1 != null) {
                    feature = FeatureData.getFeature(terrainMap[x][y].feature1);
                }
                else if (noiseMap[x][y] < -0.5 && terrainMap[x][y].feature2 != null) {
                    feature = FeatureData.getFeature(terrainMap[x][y].feature2);
                }

                squares[x][y] = new DungeonSquare(terrain, feature);
            }
        }

        // place the exits
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                squares,
                style.interiors.get(0).terrain,
                style.upstairsFeatureId,
                style.downstairsFeatureId,
                rng);

        // add items
        addItems(squares, 20, rng);

        // add the monsters
        //int numMonsters = 0;
        int numMonsters = (w - 1) * (h - 1) / 100;
        List<Actor> monsters = GeneratorUtils.createMonsters(squares, numMonsters, style.monsterIds, rng);


        GameMap map = new GameMap(name, squares, keyLocations, monsters);
        return map;
    }

    private static void addItems(DungeonSquare[][] squares, int numItems, Random rng) {
        DungeonItem softLeatherArmor = new DungeonItem(
            "& soft leather armor~",
            "soft_leather_armor",
            "soft leather armor",
            DungeonItem.Slot.ARMOR,
            new DieRoll(0,0,0),
            1,
            3,
            0,
            1,
            null);

        DungeonItem dagger = new DungeonItem(
                "& dagger~",
                "dagger",
                "a sharp dagger",
                DungeonItem.Slot.WEAPON,
                new DieRoll(2,3,1),
                1,
                0,
                0,
                1,
                null);

        DungeonItem healthPotion = new DungeonItem(
                "& red potion~",
                "red_potion",
                "tastes like fruit punch",
                DungeonItem.Slot.NONE,
                new DieRoll(0,0,0),
                0,
                0,
                0,
                1,
                new ActionParams());

        int width = Array2D.width(squares);
        int height = Array2D.height(squares);

        for (int i = 0; i < numItems; i++) {
            // find open location
            int x;
            int y;
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (squares[x][y].blockGround() || squares[x][y].feature != null || squares[x][y].items.size() > 0);

            DungeonItem item = null;
            switch (rng.nextInt(3)) {
                case 0: item = new DungeonItem(softLeatherArmor); break;
                case 1: item = new DungeonItem(dagger); break;
                case 2: item = new DungeonItem(healthPotion); break;
            }
            squares[x][y].items.add(item);
        }
    }

    // fills in squares such that the open squares are connected
    private static List<Point> findSafeInternalSquaresToBlock(int width, int height, int desiredBlocked, Random rng) {
        int[][] blocked = new int[width][height];

        // make the edges blocked initially
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocked[x][y] = (x == 0 | x == width - 1 || y == 0 || y == height - 1) ? 1 : 0;
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
        int numInteriors = style.interiors.size();

        TerrainFeatureTriplet[][] layout = new TerrainFeatureTriplet[width][height];

        // fill a border around the edge
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean isEdge = (x == 0 | x == width - 1 || y == 0 || y == height - 1);
                if (isEdge) {
                    // for now, just using first border; later extend this to multiple types.
                    layout[x][y] = mixup(style.borders.get(0), rng);
                } else {
                    layout[x][y] = mixup(style.interiors.get(rng.nextInt(numInteriors)), rng);
                }
            }
        }

        // Add squares of impassible in the middle (for dungeon shape variety)
        int x = rng.nextInt(19) + rng.nextInt(19);  // pick number from 0 to 36, 18 is most likely
        int numPossibleSquares = (width - 2)*(height - 2);
        int numDesiredToFill = (int) Math.round(x / 100.0 * numPossibleSquares);
        List<Point> safeBlocks = findSafeInternalSquaresToBlock(width, height, numDesiredToFill, rng);
        for (Point p : safeBlocks) {
            layout[p.x][p.y] = mixup(style.borders.get(0), rng);
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

//        for (int y = 0; y < Array2D.height(interpMap); y++) {
//            String line = "";
//            for (int x = 0; x < Array2D.width(interpMap); x++) {
//                line = line + interpMap[x][y] + ' ';
//            }
//            System.out.println(line);
//        }

        return interpMap;
    }

    private static TerrainFeatureTriplet[][] makeInterpMap(TerrainFeatureTriplet[][] layout, Random rng, int interpolationSteps) {
        TerrainFeatureTriplet[][] map = layout;
        for (int i = 0; i < interpolationSteps; i++) {
            map = interpolate(map, rng);
        }
        return map;
    }

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

    // removes extra borders of impassible stuff -- makes the map smaller, and
    // makes it so we won't have to "tunnel" to the nearest exit.
    private static TerrainFeatureTriplet[][] trimTerrainBorder(TerrainFeatureTriplet[][] layout, Set<String> borders) {
        int width = Array2D.width(layout);
        int height = Array2D.height(layout);

        int minInteriorX = width - 1;
        int maxInteriorX = 0;
        int minInteriorY = height - 1;
        int maxInteriorY = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!borders.contains(layout[x][y].terrain)) {
                    minInteriorX = Math.min(minInteriorX, x);
                    maxInteriorX = Math.max(maxInteriorX, x);
                    minInteriorY = Math.min(minInteriorY, y);
                    maxInteriorY = Math.max(maxInteriorY, y);
                }
            }
        }

        int newWidth = maxInteriorX - minInteriorX + 3;
        int newHeight = maxInteriorY - minInteriorY + 3;
        TerrainFeatureTriplet[][] croppedLayout = new TerrainFeatureTriplet[newWidth][newHeight];
        for (int x = minInteriorX - 1; x <= maxInteriorX + 1; x++) {
            for (int y = minInteriorY - 1; y <= maxInteriorY + 1; y++) {
                croppedLayout[x - minInteriorX + 1][y - minInteriorY + 1] = layout[x][y];
            }
        }
        return croppedLayout;
    }

    private static double[][] makeNoise(int width, int height, int origWidth, int origHeight, int interpolationSteps) {
        int scale = Math.max(origWidth, origHeight) * 2;
        double[][] noise = fractalNoise(width, height, 1.0, scale, 0.0, interpolationSteps);
        return noise;
    }
}
