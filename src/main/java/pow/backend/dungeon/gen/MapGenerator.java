package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.util.Array2D;
import pow.util.Point;

import java.util.*;

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
                    layout[x][y] = style.borders.get(0);
                } else {
                    layout[x][y] = style.interiors.get(rng.nextInt(numInteriors));
                }
            }
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


    public static GameMap genMap(
            String name,
            int width,
            int height,
            int numInterpolationSteps,
            MapStyle style,
            Map<String, String> exits,  // name of this exit -> otherAreaId@otherAreaLocName
            Random rng) {

        TerrainFeatureTriplet[][] layout = genTerrainLayout(width, height, style, rng);
        TerrainFeatureTriplet[][] terrainMap = makeInterpMap(layout, rng, numInterpolationSteps);
        int w = Array2D.width(terrainMap);
        int h = Array2D.height(terrainMap);
        List<TerrainFeatureTriplet> terrainIds = new ArrayList<>();
        terrainIds.addAll(style.borders);
        terrainIds.addAll(style.interiors);

        DungeonSquare[][] squares = new DungeonSquare[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                DungeonTerrain terrain = TerrainData.getTerrain(terrainMap[x][y].terrain);
                squares[x][y] = new DungeonSquare(terrain, null);
            }
        }

        // place the exits
        int w2 = w / 2;
        int h2 = h / 2;
        Map<String, Point> keyLocations = new HashMap<>();
        for (Map.Entry<String, String> entry : exits.entrySet()) {
            String exitName = entry.getKey();
            String target = entry.getValue();
            DungeonSquare square = buildTeleportTile(style.interiors.get(0).terrain, target);

            Point loc = new Point(-1, -1);

            switch (exitName) {
                case "north":
                    loc.x = w2;
                    loc.y = 0;
                    break;
                case "south":
                    loc.x = w2;
                    loc.y = h - 1;
                    break;
                case "west":
                    loc.x = 0;
                    loc.y = h2;
                    break;
                case "east":
                    loc.x = w - 1;
                    loc.y = h2;
                    break;
                default:
                    throw new RuntimeException("unknown exit name " + exitName);
            }

            squares[loc.x][loc.y] = square;
            keyLocations.put(exitName, loc);
        }

        int numMonsters = (w - 1) * (h - 1) / 50;
        List<Actor> monsters = DungeonGenerator.createMonsters(squares, numMonsters, style.monsterIds, rng);
        GameMap map = new GameMap(name, squares, keyLocations, monsters);
        return map;
    }
}
