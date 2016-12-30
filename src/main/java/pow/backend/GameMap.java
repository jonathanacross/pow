package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.*;
import pow.backend.actors.Monster;
import pow.backend.dungeon.gen.ShapeDLA;
import pow.backend.dungeon.gen.DungeonGenerator;
import pow.backend.dungeon.gen.GenUtils;
import pow.backend.dungeon.gen.SquareTypes;
import pow.util.Circle;
import pow.util.DebugLogger;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMap implements Serializable {
    public DungeonSquare[][] map; // indexed by x,y, or c,r

    public int width;
    public int height;
    public List<Actor> actors;
    public List<LightSource> lightSources;

    private void autogenMap(int width, int height, Random rng) {
        this.width = width;
        this.height = height;
        DungeonGenerator mapGenerator = new ShapeDLA(3, 15);

        int[][] squares = mapGenerator.genMap(width, height, rng);
        DebugLogger.info(GenUtils.getMapString(squares));

        DungeonTerrain wall = new DungeonTerrain("big stone wall", "big stone wall", "big stone wall",
                new DungeonTerrain.Flags(true));
        DungeonTerrain floor = new DungeonTerrain("floor", "floor", "floor",
                new DungeonTerrain.Flags(false));

        DungeonFeature candle = new DungeonFeature("candle", "candle", "candle",
                new DungeonFeature.Flags(false), 3);

        DungeonSquare[][] dungeonMap = new DungeonSquare[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                DungeonTerrain terrain =
                        (squares[x][y] == SquareTypes.WALL.value() ||
                        squares[x][y] == SquareTypes.CANDLEWALL.value())
                                ? wall : floor;
                DungeonFeature feature = squares[x][y] == SquareTypes.CANDLEWALL.value() ? candle : null;
                dungeonMap[x][y] = new DungeonSquare(terrain, feature);
            }
        }

        // add win/lose features
        dungeonMap[(int) (width * 0.25)][(int) (height * 0.3)].feature =
                new DungeonFeature("wintile", "way to win", "orange pearl",
                        new DungeonFeature.Flags(false), 0);
        dungeonMap[(int) (width * 0.75)][(int) (height * 0.6)].feature =
                new DungeonFeature("losetile", "death", "cobra",
                        new DungeonFeature.Flags(false), 0);

        this.map = dungeonMap;

        // a some monsters
        actors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int x = rng.nextInt(width);
            int y = rng.nextInt(height);
            if (!dungeonMap[x][y].blockGround()) {
                switch (rng.nextInt(3)) {
                    case 0: actors.add(Monster.makeBat(x,y)); break;
                    case 1: actors.add(Monster.makeRat(x,y)); break;
                    case 2: actors.add(Monster.makeSnake(x,y)); break;
                    default: break;
                }
            }
        }
    }

    public void updatePlayerVisibilityData(Player player) {
        updateBrightness();
        updateSeenLocations(player);
    }

    private void initLightSources(Player player) {
        // TODO: can objects, monsters be light sources?
        // if so, this logic will get much more complex
        this.lightSources = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map[x][y].feature == null) {
                    continue;
                }
                int radius = map[x][y].feature.lightRadius;
                if (radius > 0) {
                    this.lightSources.add(new SimpleLightSource(new Point(x,y), radius));
                }
            }
        }

        this.lightSources.add(player);
    }

    // Returns an array of values showing how bright each square is.
    // 0 = completely black; 100 = completely lit.
    // For purposes of gameplay, 0 = dark, and anything > 0 is lit
    // (i.e., the player can see).  The gradation 0-100 is primarily
    // a convenience for the frontend to display light in a cool manner.
    public static int MAX_BRIGHTNESS = 100;
    private void updateBrightness() {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y].brightness = 0;
            }
        }

        for (LightSource source: lightSources) {
            int maxR2 = Circle.getRadiusSquared(source.getLightRadius());
            int sx = source.getLocation().x;
            int sy = source.getLocation().y;
            int r = source.getLightRadius();
            int xmin = Math.max(sx - r - 1, 0);
            int ymin = Math.max(sy - r - 1, 0);
            int xmax = Math.min(sx + r + 1, width - 1);
            int ymax = Math.min(sy + r + 1, height - 1);
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    int r2 = MathUtils.dist2(x, y, sx, sy);
                    if (r2 <= maxR2) {
                        double brightness = 1.0 - (double) r2*r2 / (maxR2*maxR2);
                        map[x][y].brightness += (int) Math.round(MAX_BRIGHTNESS * brightness);
                        map[x][y].brightness = MathUtils.clamp(map[x][y].brightness, 0, MAX_BRIGHTNESS);
                    }
                }
            }
        }
    }

    public GameMap(Random rng, Player player, Pet pet) {
//        map = buildTestArea();
//        map = buildArena(40, 30, rng);
//        map = buildArena(140, 160, rng);
        autogenMap(60, 60, rng);
        initLightSources(player);

        int x = width / 2;
        int y = height / 2;
        player.loc.x = x;
        player.loc.y = y;
        player.energy.setFull(); // make sure the player can move first
        actors.add(player);
        if (pet != null) {
            pet.loc.x = x + 2;
            pet.loc.y = y + 2;
            actors.add(pet);
        }

        updatePlayerVisibilityData(player);
    }

    // update the seen locations
    private void updateSeenLocations(Player player) {
        for (Point p : Circle.getPointsInCircle(player.viewRadius)) {
            int x = p.x + player.loc.x;
            int y = p.y + player.loc.y;
            if (x >= 0 && x < width && y >= 0 && y < height) {
                if (map[x][y].brightness > 0) {
                    map[x][y].seen = true;
                }
            }
        }
    }

    private int currActorIdx;
    public void advanceActor() {
        currActorIdx = (currActorIdx + 1) % actors.size();
    }
    public Actor getCurrentActor() {
        return actors.get(currActorIdx);
    }

    public void removeActor(Actor a) {
        int idx = actors.indexOf(a);
        if (currActorIdx > idx) {
            currActorIdx--;
        }
        actors.remove(a);
    }

    public boolean isBlocked(int x, int y) {
        if (map[x][y].blockGround()) return true;
        for (Actor a: this.actors) {
            if (a.loc.x == x && a.loc.y == y && a.solid) return true;
        }
        return false;
    }

    public Actor actorAt(int x, int y) {
        for (Actor a: this.actors) {
            if (a.loc.x == x && a.loc.y == y && a.solid) return a;
        }
        return null;
    }

    private DungeonSquare[][] buildTestArea() {

        this.width = 10;
        this.height = 10;

        DungeonTerrain wall = new DungeonTerrain("big stone wall", "big stone wall", "big stone wall",
                new DungeonTerrain.Flags(true));
        DungeonTerrain floor = new DungeonTerrain("floor", "floor", "floor",
                new DungeonTerrain.Flags(false));
        DungeonSquare[][] dungeonMap = new DungeonSquare[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                dungeonMap[x][y] = (x == 0 || y == 0 || x == width-1 || y == height -1) ?
                            new DungeonSquare(wall, null) :
                            new DungeonSquare(floor, null);
            }
        }

        // a some monsters
        actors = new ArrayList<>();
        actors.add(Monster.makeBat(2,3));
        actors.add(Monster.makeRat(2,4));
        actors.add(Monster.makeSnake(2,5));
        return dungeonMap;
    }

    private DungeonSquare[][] buildArena(int width, int height, Random rng) {
        this.width = width;
        this.height = height;

        char[][] map = new char[width][height];

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                double x = c / (width - 1.0);
                double y = r / (height - 1.0);
                double d = Math.min(Math.min(x, y), Math.min(1.0 - x, 1.0 - y));
                double z = d - 0.5;
                double probWall = 16.0 * z * z * z * z;
                map[c][r] = (rng.nextDouble() < probWall) ? '#' : '.';
            }
        }

        DungeonSquare[][] dungeonMap = new DungeonSquare[width][height];
        // TODO: remove image from backend?
        DungeonTerrain wall = new DungeonTerrain("big stone wall", "big stone wall", "big stone wall",
                new DungeonTerrain.Flags(true));
        DungeonTerrain floor = new DungeonTerrain("floor", "floor", "floor",
                new DungeonTerrain.Flags(false));

        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                dungeonMap[c][r] = map[c][r] == '#' ?
                        new DungeonSquare(wall, null) :
                        new DungeonSquare(floor, null);
            }
        }

        // add win/lose features
        dungeonMap[(int) (width * 0.25)][(int) (height * 0.3)].feature =
                new DungeonFeature("wintile", "way to win", "orange pearl",
                        new DungeonFeature.Flags(false), 0);
        dungeonMap[(int) (width * 0.75)][(int) (height * 0.6)].feature =
                new DungeonFeature("losetile", "death", "cobra",
                        new DungeonFeature.Flags(false), 0);

        // a some monsters
        actors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int x = rng.nextInt(width);
            int y = rng.nextInt(height);
            if (!dungeonMap[x][y].blockGround()) {
                switch (rng.nextInt(3)) {
                    case 0: actors.add(Monster.makeBat(x,y)); break;
                    case 1: actors.add(Monster.makeRat(x,y)); break;
                    case 2: actors.add(Monster.makeSnake(x,y)); break;
                    default: break;
                }
            }
        }
        return dungeonMap;
    }
}
