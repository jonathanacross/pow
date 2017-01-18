package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.util.Array2D;
import pow.util.Point;

import java.util.*;

import static pow.util.SimplexNoise.noise;

public class MapGenerator {
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

        public MapStyle(List<TerrainFeatureTriplet> borders, List<TerrainFeatureTriplet> interiors, List<String> monsterIds) {
            this.borders = borders;
            this.interiors = interiors;
            this.monsterIds = monsterIds;
        }
    }

    private static DungeonSquare buildTeleportTile(String terrainTemplateName, String target) {
        DungeonTerrain terrainTemplate = TerrainData.getTerrain(terrainTemplateName);

        ActionParams params = new ActionParams();
        // TODO: pull out magic strings somewhere
        params.actionName = "gotoArea";
        params.name = target;
        DungeonTerrain.Flags flags = new DungeonTerrain.Flags(false, false, false, true);
        DungeonTerrain terrain = new DungeonTerrain(
                terrainTemplate.id,
                terrainTemplate.name,
                terrainTemplate.image,
                flags,
                params);

        DungeonSquare square = new DungeonSquare(terrain, null);
        return square;
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
        int numImpassible = 0;
        if (width == 4 && height == 4) {
            numImpassible = rng.nextInt(2);
        }
        else if (width == 5 && height == 5) {
            int option = rng.nextInt(10);
            if (option == 0) numImpassible = 0;
            else if (option < 4) numImpassible = 1;
            else numImpassible = 2;
        }
        else if (width > 5 && height > 5) {
            // this probably doesn't work very well, can improve later if needed
            numImpassible = rng.nextInt(width - 3);
        }

        for (int i = 0; i < numImpassible; i++) {
            int x = rng.nextInt(width - 2) + 1;
            int y = rng.nextInt(height - 2) + 1;
            layout[x][y] = mixup(style.borders.get(0), rng);
        }

        return layout;
    }

    static TerrainFeatureTriplet[][] interpolate(TerrainFeatureTriplet[][] layout, Random rng) {

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

    static TerrainFeatureTriplet[][] makeInterpMap(TerrainFeatureTriplet[][] layout, Random rng, int interpolationSteps) {
        TerrainFeatureTriplet[][] map = layout;
        for (int i = 0; i < interpolationSteps; i++) {
            map = interpolate(map, rng);
        }
        return map;
    }

    static double[][] fractalNoise(int width, int height, double initAmp, double initScale, double delta, int iters) {
        double[][] data = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double t = 0.0;
                double amp = initAmp;
                double scale = width * initScale;

                for (int i = 0; i < iters; i++) {
                    t += amp * noise(x / scale + delta, y / scale + delta);
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
    static TerrainFeatureTriplet[][] trimTerrainBorder(TerrainFeatureTriplet[][] layout, Set<String> borders) {
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

    static double[][] makeNoise(int width, int height, int interpolationSteps) {
        int areaSize = (1 << interpolationSteps);
        double[][] noise = fractalNoise(width, height, 1.0, 0.5 / areaSize, 0.0, interpolationSteps);
        return noise;
    }

    // Given a row or column to search, this returns a coordinate where there is some
    // interior square.  This will fail if there are no interior squares in this row/column.
    private static int findOtherCoordinate(TerrainFeatureTriplet[][] terrainMap, Set<String> borders,
                                           int rowOrCol, boolean vertical, Random rng) {
        List<Integer> candidates = new ArrayList<>();
        if (vertical) {
            int x = rowOrCol;
            int height = Array2D.height(terrainMap);
            for (int y = 0; y < height; y++) {
                if (!borders.contains(terrainMap[x][y].terrain)) {
                    candidates.add(y);
                }
            }
        } else {
            int y = rowOrCol;
            int width = Array2D.width(terrainMap);
            for (int x = 0; x < width; x++) {
                if (!borders.contains(terrainMap[x][y].terrain)) {
                    candidates.add(x);
                }
            }
        }

        // pick one at random
        return candidates.get(rng.nextInt(candidates.size()));
    }

    private static Point findExitCoordinates(TerrainFeatureTriplet[][] terrainMap, Set<String> borders,
                                             String side, Random rng) {
        int width = Array2D.width(terrainMap);
        int height = Array2D.height(terrainMap);

        int x;
        int y;
        switch (side) {
            case "north":
                y = 0;
                x = findOtherCoordinate(terrainMap, borders, y + 1, false, rng);
                break;
            case "south":
                y = height - 1;
                x = findOtherCoordinate(terrainMap, borders, y - 1, false, rng);
                break;
            case "west":
                x = 0;
                y = findOtherCoordinate(terrainMap, borders, x + 1, true, rng);
                break;
            case "east":
                x = width - 1;
                y = findOtherCoordinate(terrainMap, borders, x - 1, true, rng);
                break;
            default:
                x = -1;
                y = -1;
                break;
        }

        return new Point(x,y);
    }

    public static GameMap genMap(
            String name,
            int width,
            int height,
            int numInterpolationSteps,
            MapStyle style,
            Map<String, String> exits,  // name of this exit -> otherAreaId@otherAreaLocName
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
        double[][] noiseMap = makeNoise(w,h,numInterpolationSteps);

        DungeonSquare[][] squares = new DungeonSquare[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                DungeonTerrain terrain = TerrainData.getTerrain(terrainMap[x][y].terrain);

                DungeonFeature feature = null;
                if (noiseMap[x][y] > 0.7 && terrainMap[x][y].feature1 != null) {
                    feature = FeatureData.getFeature(terrainMap[x][y].feature1);
                }
                else if (noiseMap[x][y] < -0.7 && terrainMap[x][y].feature2 != null) {
                    feature = FeatureData.getFeature(terrainMap[x][y].feature2);
                }

                squares[x][y] = new DungeonSquare(terrain, feature);
            }
        }

        // place the exits
        Map<String, Point> keyLocations = new HashMap<>();
        for (Map.Entry<String, String> entry : exits.entrySet()) {
            String exitName = entry.getKey();
            String target = entry.getValue();
            DungeonSquare square = buildTeleportTile(style.interiors.get(0).terrain, target);
            Point loc = findExitCoordinates(terrainMap, borders, exitName, rng);
            squares[loc.x][loc.y] = square;
            keyLocations.put(exitName, loc);
        }

        //int numMonsters = 0;
        int numMonsters = (w - 1) * (h - 1) / 100;
        List<Actor> monsters = DungeonGenerator.createMonsters(squares, numMonsters, style.monsterIds, rng);
        GameMap map = new GameMap(name, squares, keyLocations, monsters);
        return map;
    }
}
