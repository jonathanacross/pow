package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.*;
import pow.backend.dungeon.gen.IntSquareTranslator;
import pow.backend.dungeon.gen.MonsterGenerator;
import pow.backend.dungeon.gen.proto.ShapeDLA;
import pow.backend.dungeon.gen.proto.ProtoGenerator;
import pow.backend.dungeon.gen.proto.GenUtils;
import pow.util.Array2D;
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
        ProtoGenerator mapGenerator = new ShapeDLA(3, 15);

        int[][] squares = mapGenerator.genMap(width, height, rng);
        DebugLogger.info(GenUtils.getMapString(squares));

        IntSquareTranslator translator = new IntSquareTranslator(2);

        DungeonSquare[][] dungeonMap = new DungeonSquare[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                DungeonTerrain terrain = translator.getTerrain(squares[x][y]);
                DungeonFeature feature = translator.getFeature(squares[x][y]);
                dungeonMap[x][y] = new DungeonSquare(terrain, feature);
            }
        }

        this.map = dungeonMap;

        this.actors = createMonsters(dungeonMap, 15, rng);
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

        // note this will fail if any monsters need a random number generator to create (e.g., nondeterministic HP)
        this.actors = createMonsters(dungeonMap, 3, null);
        return dungeonMap;
    }

    // creates some monsters
    private List<Actor> createMonsters(DungeonSquare[][] dungeonMap, int numMonsters, Random rng) {
        List<Actor> actors = new ArrayList<>();
        int width = Array2D.width(dungeonMap);
        int height = Array2D.height(dungeonMap);
        for (int i = 0; i < numMonsters; i++) {
            int x;
            int y;
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (dungeonMap[x][y].blockGround());
            Point location = new Point(x,y);
            String id = "";
            switch (i % 7) {
                case 0: id = "ant"; break;
                case 1: id = "yellow mushroom patch"; break;
                case 2: id = "white rat"; break;
                case 3: id = "bat"; break;
                case 4: id = "yellow snake"; break;
                case 5: id = "floating eye"; break;
                case 6: id = "chess knight"; break;
            }
            actors.add(MonsterGenerator.genMonster(id, rng, location));
        }
        return actors;
    }

}
