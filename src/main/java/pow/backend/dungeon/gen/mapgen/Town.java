package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.ShopData;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.*;
import pow.util.Array2D;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Town implements MapGenerator {

    private int level;
    private ProtoTranslator translator;
    private List<String> monsterIds;

    public Town(int level, ProtoTranslator translator, List<String> monsterIds) {
        this.level = level;
        this.translator = translator;
        this.monsterIds = monsterIds;
    }

    @Override
    public GameMap genMap(String name, List<MapConnection> connections, Random rng) {
        int[][] data = genMap(rng);

        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, translator);

        // Add exits
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
        ShopData shopData = ShopGenerator.genShop(0, rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, monsterIds, shopData);
        return map;
    }

    private int[][] genMap(Random rng) {
        int width = rng.nextInt(10) + 20;
        int height = rng.nextInt(10) + 20;

        int[][] map = new int[width][height];

        // start with an empty room
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = (x == 0 || y == 0 || x == width-1 || y == height - 1) ?
                        Constants.TERRAIN_WALL :
                        Constants.TERRAIN_FLOOR;
            }
        }

        // build 3 rectangles for buildings
        int[] doorTypes = {Constants.FEATURE_INN_DOOR, Constants.FEATURE_WEAPON_SHOP_DOOR, Constants.FEATURE_MAGIC_SHOP_DOOR};
        List<Rectangle> buildings = getBuildingLocations(map, rng, 3);
        for (int i = 0; i < 3; i++) {
            Rectangle building = buildings.get(i);
            // inset the top left of the rectangles by 1 so they guarantee to have space between them
            for (int x = building.left + 1; x < building.left + building.width; x++) {
                for (int y = building.top + 1; y < building.top + building.height; y++) {
                    map[x][y] = Constants.TERRAIN_WALL;
                }
            }

            // put doors for the shops
            int shopx = building.left + building.width/2;
            int shopy = building.top + building.height-1;
            map[shopx][shopy] = Constants.TERRAIN_FLOOR + doorTypes[i];
        }

        // light some of the outer walls
        int candleWall = Constants.TERRAIN_WALL + Constants.FEATURE_CANDLE;
        map[0][0] = candleWall;
        map[width/2][0] = candleWall;
        map[0][height/2] = candleWall;
        map[0][height-1] = candleWall;
        map[width-1][height-1] = candleWall;
        map[width/2][height-1] = candleWall;
        map[width-1][height/2] = candleWall;
        map[width-1][0] = candleWall;

        // put a fountain somewhere
        Point fountainLoc = findOpenLocation(map, rng);
        map[fountainLoc.x][fountainLoc.y] += Constants.FEATURE_FOUNTAIN;

        return map;
    }

    private Point findOpenLocation(int[][] map, Random rng) {
        int width = Array2D.width(map);
        int height = Array2D.height(map);
        int x;
        int y;
        do {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        } while (map[x][y] != Constants.TERRAIN_FLOOR);
        return new Point(x,y);
    }

    private class Rectangle {
        public int left;
        public int top;
        public int width;
        public int height;

        public Rectangle(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }

        public boolean intersects(Rectangle other) {
            boolean horizOverlap = Math.max(this.left, other.left) <= Math.min(this.left + this.width,other.left + other.width);
            boolean vertOverlap = Math.max(this.top, other.top) <= Math.min(this.top + this.height,other.top + other.height);
            return horizOverlap && vertOverlap;
        }
    }


    private List<Rectangle> getBuildingLocations(int[][] map, Random rng, int numBuildings) {
        int width = Array2D.width(map);
        int height = Array2D.height(map);

        List<Rectangle> buildings = new ArrayList<>();
        // TODO: verify that this doesn't fail (loop forever) with reasonable parameters.
        do {
            int buildingWidth = 6 + rng.nextInt(3);
            int buildingHeight = 5 + rng.nextInt(3);

            int xMin = 2 + rng.nextInt(width - 4 - buildingWidth);
            int yMin = 2 + rng.nextInt(height - 4 - buildingHeight);
            Rectangle r = new Rectangle(xMin, yMin, buildingWidth, buildingHeight);

            boolean valid = true;
            for (Rectangle prev : buildings) {
                if (r.intersects(prev)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                buildings.add(r);
            }
        } while (buildings.size() < numBuildings);

        return buildings;
    }
}
