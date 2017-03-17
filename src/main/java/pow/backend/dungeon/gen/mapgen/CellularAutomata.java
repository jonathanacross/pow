package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.*;
import pow.util.Array2D;
import pow.util.Point;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class CellularAutomata implements MapGenerator {

    private final int width;
    private final int height;
    private final int layers;
    private final boolean makeLakes;
    private final ProtoTranslator translator;
    private final int level;
    private final MonsterIdGroup monsterIds;
    private final GameMap.Flags flags;

    public CellularAutomata(int width, int height, int layers, boolean makeLakes,
                            ProtoTranslator translator, MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        this.width = width;
        this.height = height;
        this.layers = layers;
        this.makeLakes = makeLakes;
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.level = level;
        this.flags = flags;
    }

    @Override
    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {
        int[][] data = genMap(width, height, rng);
        data = GeneratorUtils.trimMap(data);
        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(data, this.translator);

        // place the exits and get key locations
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

        // add items
        int numItems = GeneratorUtils.getDefaultNumItems(data, rng);
        GeneratorUtils.addItems(level, dungeonSquares, numItems, rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), flags,null);
        return map;
    }


    private int[][] genRandom(int width, int height, double floorProb, Random rng) {

        // set up impassible rectangle by default
        int[][] data = GeneratorUtils.solidMap(width, height);

        // setup passable/impassible on the interior randomly
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (rng.nextDouble() < floorProb) {
                    data[x][y] = Constants.TERRAIN_FLOOR;
                }
            }
        }

        return data;
    }

    // runs one generation of cellular automata
    private int[][] generation(int[][] data, int r1threshold, int r2threshold) {
        int width = Array2D.width(data);
        int height = Array2D.height(data);
        int[][] newdata = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // put a wall on the border
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    newdata[x][y] = Constants.TERRAIN_WALL;
                } else {
                    // count adjacencies
                    int numAdjWallsR1;
                    int numAdjWallsR2;

                    numAdjWallsR1 = 0;
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            if (data[i][j] == Constants.TERRAIN_WALL) {
                                numAdjWallsR1++;
                            }
                        }
                    }

                    numAdjWallsR2 = 0;
                    for (int i = x - 2; i <= x + 2; i++) {
                        for (int j = y - 2; j <= y + 2; j++) {
                            if ((i < 0) || (i >= width) || (j < 0) || (j >= height)) {
                                // count off the map as a wall
                                numAdjWallsR2++;
                            } else if (data[i][j] == Constants.TERRAIN_WALL) {
                                numAdjWallsR2++;
                            }
                        }
                    }

                    if ((numAdjWallsR1 >= r1threshold) || (numAdjWallsR2 <= r2threshold)) {
                        newdata[x][y] = Constants.TERRAIN_WALL;
                    } else {
                        newdata[x][y] = Constants.TERRAIN_FLOOR;
                    }
                }
            }
        }

        return newdata;
    }

    private int[][] genConnected(int width, int height, Random rng) {
        int[][] data;
        int connSize = 0;

        do {
            data = genRandom(width, height, 0.55, rng);

            // run the cellular automaton for a few generations
            for (int i = 0; i < 5; i++) {
                data = generation(data, 5, 3);
            }
            for (int i = 0; i < 3; i++) {
                data = generation(data, 5, -1);
            }

            // check connectedness
            Point openloc = GeneratorUtils.findOpenSpace(data);
            if (openloc.x < 0) {
                continue;
            }

            GeneratorUtils.floodFill(data, openloc.x, openloc.y, Constants.TERRAIN_TEMP, Constants.TERRAIN_FLOOR);

            // remove regions not connected to main region, and measure the
            // size of the main region
            connSize = 0;
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    if (data[x][y] == Constants.TERRAIN_TEMP) {
                        data[x][y] = Constants.TERRAIN_FLOOR;
                        connSize++;
                    } else if (data[x][y] == Constants.TERRAIN_FLOOR) {
                        data[x][y] = Constants.TERRAIN_WALL;
                    }
                }
            }
        } while ((double) connSize / (width * height) < 0.4); // make sure the dungeon is sufficiently big

        return data;
    }

    // finds where to put lakes.
    // Resulting lakes will be marked as floor in the returned map
    private int[][] makeLakes(int[][] map, int wallSteps) {

        // expand the walls a few times
        int[][] lakeAreas = generation(map, 1, -1);
        for (int i = 1; i < wallSteps; i++) {
            lakeAreas = generation(lakeAreas, 1, -1);
        }
        // re-expand the lakes once to make them more circular
        lakeAreas = generation(lakeAreas, 9, 8);

        return lakeAreas;
    }

    private int[][] genMap(int width, int height, Random rng) {

        // the main (explorable) layer must be connected
        int[][] connData = genConnected(width, height, rng);

        if (this.layers > 1) {
            // for 2 or 3 layers, don't worry about connectivity, just
            // do the CA directly
            int[][] wallData = genRandom(width, height, 0.55, rng);

            for (int i = 0; i < 10; i++) {
                wallData = generation(wallData, 5, -1);
            }

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if ((connData[x][y] == Constants.TERRAIN_FLOOR) && (wallData[x][y] == Constants.TERRAIN_WALL)) {
                        connData[x][y] = Constants.TERRAIN_DIGGABLE_WALL;
                    }
                    if ((this.layers == 3) && (connData[x][y] == Constants.TERRAIN_WALL) && (wallData[x][y] == Constants.TERRAIN_FLOOR)) {
                        connData[x][y] = Constants.TERRAIN_LAVA;
                    }
                }
            }
        }

        if (this.makeLakes) {
            int[][] lakes = makeLakes(connData, 4);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (lakes[x][y] == Constants.TERRAIN_FLOOR) {
                        connData[x][y] = Constants.TERRAIN_WATER;
                    }
                }
            }
        }

        return connData;
    }
}
