package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.GameConstants;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.*;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.util.Array2D;
import pow.util.Direction;
import pow.util.Point;

import java.util.*;

public class GeneratorUtils {

    // Common ids used for GenerateCommonExits to simplify signature.
    public static class CommonIds {
        public final String interiorTerrain; // interior terrain id used to make a N/S/E/W exit.
        public final String upstairsFeature;
        public final String downstairsFeature;
        public final String openPortalFeature;
        public final String closedPortalFeature;

        public CommonIds(String interiorTerrain, String upstairsFeature, String downstairsFeature,
                                String openPortalFeature, String closedPortalFeature) {
            this.interiorTerrain = interiorTerrain;
            this.upstairsFeature = upstairsFeature;
            this.downstairsFeature = downstairsFeature;
            this.openPortalFeature = openPortalFeature;
            this.closedPortalFeature = closedPortalFeature;
        }
    }

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
    public static void floodFill(int[][] data, int lx, int ly, int newColor, int oldColor) {

        if (oldColor == newColor) {
            return;
        }

        int w = Array2D.width(data);
        int h = Array2D.height(data);

        Stack<Point> stack = new Stack<>();
        stack.add(new Point(lx, ly));

        while (stack.size() > 0) {
            Point loc = stack.pop();
            int x = loc.x;
            int y1 = loc.y;
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
                sb.append(Constants.getChar(map[x][y]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Finds the location of 'value' in the array.
    // If it doesn't exist, returns (-1,-1).
    private static Point findValue(int[][] data, int value) {
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

    private static List<Actor> createMonsters(DungeonSquare[][] dungeonMap,
                                             double density,  // # monsters per square
                                             MonsterIdGroup monsterIdGroup,
                                             Random rng) {

        assert(density < 1);
        int width = Array2D.width(dungeonMap);
        int height = Array2D.height(dungeonMap);

        List<Point> availableGroundSquares = new ArrayList<>();
        List<Point> availableWaterSquares = new ArrayList<>();
        // Skip outer edge, since we don't want to put monsters right on
        // exits to other levels, where player may come in.
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (!dungeonMap[x][y].blockGround()) { availableGroundSquares.add(new Point(x,y)); }
                if (!dungeonMap[x][y].blockWater()) { availableWaterSquares.add(new Point(x,y)); }
            }
        }

        int numGroundMonsters = (int) Math.round(density * availableGroundSquares.size());
        int numWaterMonsters = (int) Math.round(density * availableWaterSquares.size());

        List<Actor> actors = new ArrayList<>();

        // place the boss, if any.  For now, assuming boss will go on land
        if (monsterIdGroup.canGenBoss) {
            int idx = rng.nextInt(availableGroundSquares.size());
            Point location = availableGroundSquares.get(idx);
            String id = monsterIdGroup.bossId;
            actors.add(MonsterGenerator.genMonster(id, rng, location));
            availableGroundSquares.remove(idx);
        }

        // place ground monsters
        List<String> groundIds = monsterIdGroup.getGroundMonsterIds();
        if (!groundIds.isEmpty()) {
            for (int i = 0; i < numGroundMonsters; i++) {
                int idx = rng.nextInt(availableGroundSquares.size());
                Point location = availableGroundSquares.get(idx);
                String id = groundIds.get(rng.nextInt(groundIds.size()));
                actors.add(MonsterGenerator.genMonster(id, rng, location));
                availableGroundSquares.remove(idx);
            }
        }

        // place water monsters
        List<String> waterIds = monsterIdGroup.getWaterMonsterIds();
        if (!waterIds.isEmpty()) {
            for (int i = 0; i < numWaterMonsters; i++) {
                int idx = rng.nextInt(availableWaterSquares.size());
                Point location = availableWaterSquares.get(idx);
                String id = waterIds.get(rng.nextInt(waterIds.size()));
                actors.add(MonsterGenerator.genMonster(id, rng, location));
                availableWaterSquares.remove(idx);
            }
        }

        return actors;
    }

    // note: this also removes the player and pet from the map!
    public static void regenMonstersForCurrentMap(GameMap map, Random rng) {
        map.actors = createMonsters(map.map, GameConstants.MONSTER_DENSITY, map.genMonsterIds, rng);
    }

    public static void healAllMonsters(GameMap map) {
        for (Actor a : map.actors) {
            if (!a.friendly) {
                a.setFullHealth();
                a.setFullMana();
            }
        }
    }

    // debug method: puts one of every type of monster in the level
    public static List<Actor> createMonstersOrdered(DungeonSquare[][] dungeonMap, Random rng) {
        List<Actor> actors = new ArrayList<>();
        int width = Array2D.width(dungeonMap);
        int height = Array2D.height(dungeonMap);

        // to make sure we don't put monsters on top of each other
        boolean[][] monsterAt = new boolean[width][height];

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

    // We require a 1 square border around a stair, so that they may be entered from
    // any direction.
    // Assumes that the indices x,y are completely within the bounds (with 1 square buffer).
    private static boolean stairsAllowedAt(DungeonSquare[][] squares, int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                DungeonSquare square = squares[x + dx][y + dy];
                if (square.feature != null || square.blockGround()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Point findStairsLocation(DungeonSquare[][] squares, Random rng) {
        int width = Array2D.width(squares);
        int height = Array2D.height(squares);
        int x;
        int y;
        do {
            x = rng.nextInt(width - 2) + 1;
            y = rng.nextInt(height - 2) + 1;
        } while (!stairsAllowedAt(squares, x, y));
        return new Point(x,y);
    }

    // makes a feature for up/down stairs (or into dungeon)
    private static DungeonFeature buildStairsFeature(String upFeatureId, String downFeatureId, MapConnection connection) {
        boolean up = connection.dir == Direction.U;
        String featureId = up ? upFeatureId : downFeatureId;
        DungeonFeature featureTemplate = FeatureData.getFeature(featureId);
        // Set the flags in case not set in the file. We may even be able to remove such
        // flags from the file.
        ActionParams params = new ActionParams();
        params.actionName = ActionParams.ActionName.MOVE_TO_AREA_ACTION;
        params.name = connection.destination.toString();
        DungeonFeature.Flags flags =  new DungeonFeature.Flags(
                true,
                true,
                false,
                false,
                true,
                up,
                !up,
                false,
                true);
        return new DungeonFeature(
                featureTemplate.id,
                featureTemplate.name,
                featureTemplate.image,
                flags,
                params);
    }

    private static DungeonSquare buildTeleportTile(String terrainTemplateName, String target) {
        DungeonTerrain terrainTemplate = TerrainData.getTerrain(terrainTemplateName);

        ActionParams params = new ActionParams();
        params.actionName = ActionParams.ActionName.MOVE_TO_AREA_ACTION;
        params.name = target;
        DungeonTerrain.Flags flags = new DungeonTerrain.Flags(false, false, false, false, false, true);
        DungeonTerrain terrain = new DungeonTerrain(
                terrainTemplate.id,
                terrainTemplate.name,
                terrainTemplate.image,
                flags,
                params);

        return new DungeonSquare(terrain, null);
    }

    private static DungeonFeature buildPortalFeature(
            String openPortalFeatureId, String closedPortalFeatureId,
            boolean isOpen) {
        String featureId = isOpen ? openPortalFeatureId : closedPortalFeatureId;
        DungeonFeature featureTemplate = FeatureData.getFeature(featureId);

        ActionParams params = new ActionParams();
        if (isOpen) {
            params.actionName = ActionParams.ActionName.DEBUG_ACTION;
            params.name = "OPEN_PORTAL_DEBUG";
        } else {
            params.actionName = ActionParams.ActionName.DEBUG_ACTION;
            params.name = "CLOSED_PORTAL_DEBUG";
        }
        DungeonFeature.Flags flags =  new DungeonFeature.Flags(
                true,
                true,
                false,
                true,
                true,
                false,
                false,
                false,
                true);

        return new DungeonFeature(
                featureTemplate.id,
                featureTemplate.name,
                featureTemplate.image,
                flags,
                params);
    }

    public static int getDefaultNumItems(int width, int height, Random rng) {
        int area = width * height;
        double meanNumItems = GameConstants.ITEM_DENSITY * area;

        int numItems = (int) Math.round(3 * rng.nextGaussian() + meanNumItems);
        numItems = Math.max(0, numItems);
        return numItems;
    }

    public static int getDefaultNumItems(int[][] squares, Random rng) {
        return getDefaultNumItems( Array2D.width(squares), Array2D.height(squares), rng);
    }

    // Removes extra borders of impassible stuff -- makes the map smaller, and
    // makes it so we won't have to "tunnel" to the nearest exit.
    // This is necessary to call before using findExitCoordinate.
    public static int[][] trimMap(int[][] squares) {
        int width = Array2D.width(squares);
        int height = Array2D.height(squares);

        int minInteriorX = width - 1;
        int maxInteriorX = 0;
        int minInteriorY = height - 1;
        int maxInteriorY = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Constants.getTerrain(squares[x][y]) != Constants.TERRAIN_WALL) {
                    minInteriorX = Math.min(minInteriorX, x);
                    maxInteriorX = Math.max(maxInteriorX, x);
                    minInteriorY = Math.min(minInteriorY, y);
                    maxInteriorY = Math.max(maxInteriorY, y);
                }
            }
        }

        int newWidth = maxInteriorX - minInteriorX + 3;
        int newHeight = maxInteriorY - minInteriorY + 3;
        int[][] croppedLayout = new int[newWidth][newHeight];
        for (int x = minInteriorX - 1; x <= maxInteriorX + 1; x++) {
            for (int y = minInteriorY - 1; y <= maxInteriorY + 1; y++) {
                croppedLayout[x - minInteriorX + 1][y - minInteriorY + 1] = squares[x][y];
            }
        }
        return croppedLayout;
    }

    private static boolean isOpen(DungeonSquare square) {
        return !square.terrain.flags.blockGround || !square.terrain.flags.blockWater || square.terrain.flags.diggable;
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
    private static Point findExitCoordinates(
            DungeonSquare[][] squares,
            Direction direction,
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
    //    * There are 3x3 spots available to place any up/down exits
    //      and portals.
    public static Map<String, Point> addDefaultExits(
            List<MapConnection> connections,
            MapPoint.PortalStatus portalStatus,
            DungeonSquare[][] squares,  // modified in place
            CommonIds ids,
            Random rng) {
        Map<String, Point> keyLocations = new HashMap<>();
        for (MapConnection connection : connections) {
            if (connection.dir == Direction.U || connection.dir == Direction.D) {
                // up or down
                DungeonFeature stairs = GeneratorUtils.buildStairsFeature(ids.upstairsFeature, ids.downstairsFeature, connection);
                Point loc = GeneratorUtils.findStairsLocation(squares, rng);
                squares[loc.x][loc.y].feature = stairs;
                keyLocations.put(connection.name, loc);
            } else {
                // cardinal direction
                DungeonSquare square = GeneratorUtils.buildTeleportTile(ids.interiorTerrain, connection.destination.toString());
                Point loc = GeneratorUtils.findExitCoordinates(squares, connection.dir, rng);
                squares[loc.x][loc.y] = square;
                keyLocations.put(connection.name, loc);
            }
        }
        if (portalStatus != MapPoint.PortalStatus.NONE) {
            boolean isOpen = (portalStatus == MapPoint.PortalStatus.OPEN);
            DungeonFeature portal = GeneratorUtils.buildPortalFeature(ids.openPortalFeature, ids.closedPortalFeature, isOpen);
            Point loc = GeneratorUtils.findStairsLocation(squares, rng);
            squares[loc.x][loc.y].feature = portal;
            // TODO: pull out this string constant
            keyLocations.put("portal", loc);
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
            } while (
                    (squares[x][y].blockGround() && squares[x][y].blockWater()) ||     // don't put items where player can't go
                    squares[x][y].feature != null ||   // don't put items over a feature
                    squares[x][y].items.size() > 0);   // don't put items on top of items

            DungeonItem item = getRandomItemForLevel(level, rng);
            squares[x][y].items.add(item);
        }
    }

    public static DungeonItem getRandomItemForLevel(int level, Random rng) {
        int perturbedLevel = (int) Math.round(2 * rng.nextGaussian() + level);
        List<String> possibleItemIds = ItemGenerator.getItemIdsForLevel(perturbedLevel);
        String itemId = possibleItemIds.get(rng.nextInt(possibleItemIds.size()));
        return ItemGenerator.genItem(itemId, perturbedLevel, rng);
    }

    public static DungeonItem getRandomMoneyForLevel(int level, Random rng) {
        int perturbedLevel = (int) Math.round(2 * rng.nextGaussian() + level);
        List<String> possibleItemIds = ItemGenerator.getMoneyIdsForLevel(perturbedLevel);
        String itemId = possibleItemIds.get(rng.nextInt(possibleItemIds.size()));
        return ItemGenerator.genItem(itemId, perturbedLevel, rng);
    }
}
