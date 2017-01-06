package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.*;
import pow.backend.dungeon.gen.DungeonGenerator;
import pow.backend.dungeon.gen.ProtoTranslator;
import pow.backend.dungeon.gen.proto.ShapeDLA;
import pow.backend.dungeon.gen.proto.ProtoGenerator;
import pow.util.Circle;
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
                DungeonFeature f = map[x][y].feature;
                if (f != null && f.flags.glowing) {
                    this.lightSources.add(new SimpleLightSource(new Point(x,y), 3));
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
        ProtoGenerator generator = new ShapeDLA(3, 15);
        ProtoTranslator translator = new ProtoTranslator(2);
        buildTestArea();
        this.height = 60;
        this.width = 60;
//        this.map = buildTestArea();
        this.map = DungeonGenerator.generateMap(generator, translator, this.width, this.height, rng);
        this.actors = DungeonGenerator.createMonsters(this.map, 50, rng);
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

        this.width = 15;
        this.height = 15;

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
        this.actors = DungeonGenerator.createMonstersOrdered(dungeonMap, null);
        //this.actors = DungeonGenerator.createMonsters(dungeonMap, 3, null);
        return dungeonMap;
    }
}
