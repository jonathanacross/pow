package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.util.Array2D;
import pow.util.DebugLogger;
import pow.util.Point;

import java.util.*;

public class GeneratorUtils {

    // generates a solid wall of dungeon of the desired size
    public static int[][] solidMap(int width, int height) {

        int[][] data = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data[x][y] = Constants.TERRAIN_WALL;
            }
        }

        return data;
    }

    // adapted from http://lodev.org/cgtutor/floodfill.html
    public static int[][] floodFill(int[][] data, int lx, int ly, int newColor, int oldColor) {

        if (oldColor == newColor) {
            return data;
        }

        int w = Array2D.width(data);
        int h = Array2D.height(data);

        Stack<Point> stack = new Stack<>();
        stack.add(new Point(lx, ly));

        while (stack.size() > 0) {
            Point loc = stack.pop();
            int x = loc.x;
            int y = loc.y;
            int y1 = y;
            while (y1 >= 0 && data[x][y1] == oldColor) {
                y1--;
            }
            y1++;
            boolean spanLeft = false;
            boolean spanRight = false;
            while (y1 < h && data[x][y1] == oldColor) {
                data[x][y1] = newColor;
                if (!spanLeft && x > 0 && data[x - 1][y1] == oldColor) {
                    stack.add(new Point(x - 1, y1));
                    spanLeft = true;
                } else if (spanLeft && x > 0 && data[x - 1][y1] != oldColor) {
                    spanLeft = false;
                }
                if (!spanRight && x < w - 1 && data[x + 1][y1] == oldColor) {
                    stack.add(new Point(x + 1, y1));
                    spanRight = true;
                } else if (spanRight && x < w - 1 && data[x + 1][y1] != oldColor) {
                    spanRight = false;
                }
                y1++;
            }
        }

        return data;
    }

    public static Point findOpenSpace(int[][] data) {
        int h = Array2D.height(data);
        int w = Array2D.width(data);

        // Search starting at the middle, then go back
        // to the top -- if we start at the top, then often
        // the first several rows will not be open, and
        // this will waste time going through such squares.
        int mid = h / 2;

        for (int y = mid; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (data[x][y] == Constants.TERRAIN_FLOOR) return new Point(x, y);
            }
        }
        for (int y = 0; y < mid; y++) {
            for (int x = 0; x < w; x++) {
                if (data[x][y] == Constants.TERRAIN_FLOOR) return new Point(x, y);
            }
        }

        // no point found; return somewhere off the map
        return new Point(-1, -1);
    }

    // useful to draw debug maps
    public static String getMapString(int[][] map) {
        int height = Array2D.height(map);
        int width = Array2D.width(map);

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(getChar(map[x][y]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static char getChar(int x) {
        int feature = Constants.getFeature(x);
        // if there's a feature, draw it
        if (feature != Constants.FEATURE_NONE) {
            switch (feature) {
                case Constants.FEATURE_CLOSED_DOOR: return '+';
                case Constants.FEATURE_OPEN_DOOR: return '\'';
                case Constants.FEATURE_CANDLE: return 'c';
                case Constants.FEATURE_WIN_TILE: return 'W';
                case Constants.FEATURE_LOSE_TILE: return 'L';
                case Constants.FEATURE_UP_STAIRS: return '<';
                case Constants.FEATURE_DOWN_STAIRS: return '>';
                default: throw new IllegalArgumentException("unknown feature " + feature);
            }
        } else {
            // draw the terrain
            int terrain = Constants.getTerrain(x);
            switch (terrain) {
                case Constants.TERRAIN_WALL: return '#';
                case Constants.TERRAIN_FLOOR: return '.';
                case Constants.TERRAIN_DIGGABLE_WALL: return '%';
                case Constants.TERRAIN_LAVA: return '~';
                case Constants.TERRAIN_WATER: return 'w';
                case Constants.TERRAIN_DEBUG: return '?';
                default: throw new IllegalArgumentException("unknown terrain " + terrain);
            }
        }
    }

    // Finds the location of 'value' in the array.
    // If it doesn't exist, returns (-1,-1).
    public static Point findValue(int[][] data, int value) {
        int h = Array2D.height(data);
        int w = Array2D.width(data);

        // Search starting at the middle, then go back
        // to the top -- if we start at the top, then often
        // the first several rows will not be open, and
        // this will waste time going through such squares.
        int mid = h / 2;

        for (int y = mid; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (data[x][y] == value) return new Point(x, y);
            }
        }
        for (int y = 0; y < mid; y++) {
            for (int x = 0; x < w; x++) {
                if (data[x][y] == value) return new Point(x, y);
            }
        }

        // no point found; return somewhere off the map
        return new Point(-1, -1);
    }

    // returns true if the region of squares with value 'value' is
    // connected.  Returns false if the region doesn't exist, or
    // if not connected.
    public static boolean hasConnectedRegionWithValue(int[][] data, int value) {
        Point startLoc = findValue(data, value);
        if (startLoc.x < 0) {
            return false;
        }

        // temporarily fill the region with another color
        int tmp = Integer.MIN_VALUE;
        floodFill(data, startLoc.x, startLoc.y, tmp, value);

        // if connected, then we shouldn't be able to find any more
        // squares with 'value'
        Point startLoc2 = findValue(data, value);
        boolean connected = startLoc2.x < 0;

        // restore the flood-fill area
        floodFill(data, startLoc.x, startLoc.y, value, tmp);

        return connected;
    }

    public static DungeonSquare[][] convertToDungeonSquares(int[][] squares, ProtoTranslator translator) {
        int w = Array2D.width(squares);
        int h = Array2D.height(squares);
        DebugLogger.info(GeneratorUtils.getMapString(squares));

        DungeonSquare[][] dungeonMap = new DungeonSquare[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                DungeonTerrain terrain = translator.getTerrain(squares[x][y]);
                DungeonFeature feature = translator.getFeature(squares[x][y]);
                dungeonMap[x][y] = new DungeonSquare(terrain, feature);
            }
        }

        return dungeonMap;
    }

    // Generates from a subset of monsters.
    // If monsterIds == null, then generate from all possible monsters
    public static List<Actor> createMonsters(DungeonSquare[][] dungeonMap, int numMonsters, List<String> monsterIds, Random rng) {
        List<Actor> actors = new ArrayList<>();
        int width = Array2D.width(dungeonMap);
        int height = Array2D.height(dungeonMap);

        // to make sure we don't put monsters on top of each other
        boolean[][] monsterAt = new boolean[width][height];

        if (monsterIds == null) {
            monsterIds = new ArrayList<>();
            monsterIds.addAll(MonsterGenerator.getMonsterIds());
        }

        for (int i = 0; i < numMonsters; i++) {
            int x;
            int y;
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (dungeonMap[x][y].blockGround() || monsterAt[x][y]);
            Point location = new Point(x,y);

            String id = monsterIds.get(rng.nextInt(monsterIds.size()));
            actors.add(MonsterGenerator.genMonster(id, rng, location));
            monsterAt[location.x][location.y] = true;
        }
        return actors;
    }

    // debug method: puts one of every type of monster in the level
    public static List<Actor> createMonstersOrdered(DungeonSquare[][] dungeonMap, Random rng) {
        List<Actor> actors = new ArrayList<>();
        int width = Array2D.width(dungeonMap);
        int height = Array2D.height(dungeonMap);

        // to make sure we don't put monsters on top of each other
        boolean[][] monsterAt = new boolean[width][height];

        List<String> monsterIds = new ArrayList<>();
        monsterIds.addAll(MonsterGenerator.getMonsterIds());

        int x = 0;
        int y = 0;
        for (String monster : MonsterGenerator.getMonsterIds()) {
            do {
                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            } while (dungeonMap[x][y].blockGround() || monsterAt[x][y]);
            Point location = new Point(x,y);

            actors.add(MonsterGenerator.genMonster(monster, rng, location));
            monsterAt[location.x][location.y] = true;
        }
        return actors;
    }

    // --------------------- related to exits --------------

    public static Point findStairsLocation(DungeonSquare[][] squares, Random rng) {
        int width = Array2D.width(squares);
        int height = Array2D.width(squares);
        int x;
        int y;
        do {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        } while (squares[x][y].feature != null || squares[x][y].blockGround());
        return new Point(x,y);
    }

    // makes a feature for up/down stairs (or into dungeon)
    public static DungeonFeature buildStairsFeature(String upFeatureId, String downFeatureId, MapConnection connection) {
        boolean up = connection.dir == MapConnection.Direction.U;
        String featureId = up ? upFeatureId : downFeatureId;
        DungeonFeature featureTemplate = FeatureData.getFeature(featureId);
        // Set the flags in case not set in the file. We may even be able to remove such
        // flags from the file.
        ActionParams params = new ActionParams();
        // TODO: pull out magic strings somewhere
        params.actionName = "gotoArea";
        params.name = connection.destination.toString();
        DungeonFeature.Flags flags =  new DungeonFeature.Flags(false, false, false, up, !up) ;
        DungeonFeature feature = new DungeonFeature(
                featureTemplate.id,
                featureTemplate.name,
                featureTemplate.image,
                flags,
                params);

        return feature;
    }

    public static DungeonSquare buildTeleportTile(String terrainTemplateName, String target) {
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

    private static boolean isOpen(DungeonSquare square) {
        return !square.terrain.flags.blockGround;// && square.feature == null;
    }

    // Given a row or column to search, this returns a coordinate where there is some
    // interior square.  This will fail if there are no interior squares in this row/column.
    private static int findOtherCoordinate(DungeonSquare[][] squares,
                                           int rowOrCol, boolean vertical, Random rng) {
        List<Integer> candidates = new ArrayList<>();
        if (vertical) {
            int x = rowOrCol;
            int height = Array2D.height(squares);
            for (int y = 0; y < height; y++) {
                if (isOpen(squares[x][y])) {
                    candidates.add(y);
                }
            }
        } else {
            int y = rowOrCol;
            int width = Array2D.width(squares);
            for (int x = 0; x < width; x++) {
                if (isOpen(squares[x][y])) {
                    candidates.add(x);
                }
            }
        }

        // pick one at random
        return candidates.get(rng.nextInt(candidates.size()));
    }

    // Utility method to find a location to put a square exiting a map.
    // This requires that the map has a border of at most one square thick
    // at some point on each side of the map.  It works by picking one of
    // the locations on the side of interest where the interior is one
    // square from the edge.
    public static Point findExitCoordinates(
            DungeonSquare[][] squares,
            MapConnection.Direction direction,
            Random rng) {
        int width = Array2D.width(squares);
        int height = Array2D.height(squares);

        int x;
        int y;
        switch (direction) {
            case N:
                y = 0;
                x = findOtherCoordinate(squares, y + 1, false, rng);
                break;
            case S:
                y = height - 1;
                x = findOtherCoordinate(squares, y - 1, false, rng);
                break;
            case W:
                x = 0;
                y = findOtherCoordinate(squares, x + 1, true, rng);
                break;
            case E:
                x = width - 1;
                y = findOtherCoordinate(squares, x - 1, true, rng);
                break;
            default:
                x = -1;
                y = -1;
                break;
        }

        return new Point(x,y);
    }

    // Function to add exits to a map.
    // It modifies the array of DungeonSquares in place, adding features,
    // or replacing the squares completely to put exits.  This function
    // returns the list of key locations.
    //
    // This function makes multiple assumptions:
    //    * The map has been trimmed--that is, there is a spot on each
    //      edge where the border is only one square thick.
    //    * There is at most 1 connection in each cardinal direction
    //      (There may be multiple up/down connections)
    public static Map<String, Point> addDefaultExits(
            List<MapConnection> connections,
            DungeonSquare[][] squares,  // modified in place
            String interiorTerrainId, // interior terrain id used to make a N/S/E/W exit.
            String upstairsFeatureId,  // feature ids used to make upstairs or downstairs.
            String downstairsFeatureId,
            Random rng) {
        Map<String, Point> keyLocations = new HashMap<>();
        for (MapConnection connection : connections) {
            if (connection.dir == MapConnection.Direction.U ||
                    connection.dir == MapConnection.Direction.D) {
                // up or down
                DungeonFeature stairs = GeneratorUtils.buildStairsFeature(upstairsFeatureId, downstairsFeatureId, connection);
                Point loc = GeneratorUtils.findStairsLocation(squares, rng);
                squares[loc.x][loc.y].feature = stairs;
                keyLocations.put(connection.name, loc);
            } else {
                // cardinal direction
                DungeonSquare square = GeneratorUtils.buildTeleportTile(interiorTerrainId, connection.destination.toString());
                Point loc = GeneratorUtils.findExitCoordinates(squares, connection.dir, rng);
                squares[loc.x][loc.y] = square;
                keyLocations.put(connection.name, loc);
            }
        }
        return keyLocations;

    }

    public static void addItems(int level, DungeonSquare[][] squares, int numItems, Random rng) {
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

            int perturbedLevel = (int) Math.round(2 * rng.nextGaussian() + level);

            List<String> possibleItemIds = ItemGenerator.getItemIdsForLevel(perturbedLevel);
            String itemId = possibleItemIds.get(rng.nextInt(possibleItemIds.size()));
            DungeonItem item = ItemGenerator.genItem(itemId, perturbedLevel, rng);

            squares[x][y].items.add(item);
        }
    }

}
