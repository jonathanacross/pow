package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.LightSource;
import pow.backend.dungeon.SimpleLightSource;
import pow.backend.dungeon.MonsterIdGroup;
import pow.util.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameMap implements Serializable {

    public static class Flags implements Serializable {
        boolean permLight; // should the level always be lit?
        boolean outside;  // if outside, then we can illuminate based on day/night
        boolean poisonGas;  // player loses health if not wearing gasmask
        boolean hot;  // player loses health if not wearing heatsuit

        public Flags(boolean permLight, boolean outside, boolean poisonGas, boolean hot) {
            this.permLight = permLight;
            this.outside = outside;
            this.poisonGas = poisonGas;
            this.hot = hot;
        }
    }

    public final DungeonSquare[][] map; // indexed by x,y, or c,r

    public final int width;
    public final int height;
    public final MonsterIdGroup genMonsterIds;  // monsters to generate for this level
    public List<Actor> actors;
    private List<LightSource> lightSources;
    public final Map<String, Point> keyLocations;  // useful for joining areas together
    public final String name; // name of the area
    public final int level;  // difficulty level
    public ShopData shopData; // for stores contained in this map
    public final Flags flags;

    public void updatePlayerVisibilityData(Player player) {
        updateBrightness(player);
        updateSeenLocations(player);
    }

    private void initLightSources() {
        // TODO: can objects, monsters be light sources?
        // if so, this logic will get much more complex
        this.lightSources = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                DungeonFeature f = map[x][y].feature;
                if (f != null && f.flags.glowing) {
                    this.lightSources.add(new SimpleLightSource(new Point(x,y), GameConstants.CANDLE_LIGHT_RADIUS));
                }
            }
        }
    }

    // helper method for updateBrightness
    private void addBrightness(LightSource lightSource) {
        int maxR2 = Circle.getRadiusSquared(lightSource.getLightRadius());
        int sx = lightSource.getLocation().x;
        int sy = lightSource.getLocation().y;
        int r = lightSource.getLightRadius();
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

    // Returns an array of values showing how bright each square is.
    // 0 = completely black; 100 = completely lit.
    // For purposes of gameplay, 0 = dark, and anything > 0 is lit
    // (i.e., the player can see).  The gradation 0-100 is primarily
    // a convenience for the frontend to display light in a cool manner.
    public static final int MAX_BRIGHTNESS = 100;
    private void updateBrightness(Player player) {
        if (flags.permLight) {
            // level completely lit
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    map[x][y].brightness = MAX_BRIGHTNESS;
                }
            }
            return;
        }

        // level is dark
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y].brightness = 0;
            }
        }

        for (LightSource source: lightSources) {
            addBrightness(source);
        }
        addBrightness(player);
    }

    public GameMap(String name,
                   int level,
                   DungeonSquare[][] map,
                   Map<String, Point> keyLocations,
                   MonsterIdGroup genMonsterIds,
                   Flags flags,
                   ShopData shopData) {
        this.name = name;
        this.level = level;
        this.map = map;
        this.height = Array2D.height(this.map);
        this.width = Array2D.width(this.map);
        this.keyLocations = keyLocations;
        this.genMonsterIds = genMonsterIds;
        this.actors = new ArrayList<>();
        this.flags = flags;
        this.shopData = shopData;
        initLightSources();
    }

    // Call when a player enters a level.
    // Player is set to the requested location, and set to full energy.
    // Pet is moved as near to the player as possible.
    public void placePlayerAndPet(Player player, Point playerLoc, Pet pet) {
        player.loc.x = playerLoc.x;
        player.loc.y = playerLoc.y;
        addActor(player);
        player.energy.setFull(); // make sure the player can move first

        if (pet != null) {
            if (hasOpenSquare(pet)) {
                addActor(pet);
                pet.loc = findClosestOpenSquare(player, playerLoc);
            }
            else {
                // TODO: log that the pet can't join..
            }
        }

        updatePlayerVisibilityData(player);
    }

    // update the seen locations
    private void updateSeenLocations(Player player) {
        for (Point p : Circle.getPointsInCircle(player.viewRadius)) {
            int x = p.x + player.loc.x;
            int y = p.y + player.loc.y;
            if (isOnMap(x,y)) {
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

    private void addActor(Actor a) {
        actors.add(a);
    }

    public void removeActor(Actor a) {
        int idx = actors.indexOf(a);
        if (currActorIdx > idx) {
            currActorIdx--;
        }
        actors.remove(a);
    }

    public boolean isOnMap(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isBlocked(Actor actor, int x, int y) {
        if (!isOnMap(x,y)) return true;
        boolean terrainMatches =
                (actor.terrestrial && !map[x][y].blockGround()) ||
                (actor.aquatic && !map[x][y].blockWater());
        if (! terrainMatches) return true;
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

    // Checks to make sure the level has a place where the
    // actor can be placed
    private boolean hasOpenSquare(Actor actor) {
        int width = Array2D.width(map);
        int height = Array2D.height(map);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!isBlocked(actor, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Finds the closest open square to the starting location.
    // This assumes that there is at least one open square.
    private Point findClosestOpenSquare(Actor actor, Point start) {
        int i = 0;
        Point loc;
        do {
            loc = Spiral.position(i);
            loc.shiftBy(start);
            i++;
        } while (isBlocked(actor, loc.x, loc.y));

        return loc;
    }

    // Assumes that there is at least one open point.
    public Point findRandomOpenSquare(Random rng) {
        int x;
        int y;
        do {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        } while (map[x][y].blockGround());
        return new Point(x,y);
    }
}
