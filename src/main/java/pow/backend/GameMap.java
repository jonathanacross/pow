package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.LightSource;
import pow.backend.dungeon.SimpleLightSource;
import pow.backend.dungeon.MonsterIdGroup;
import pow.util.*;

import java.io.Serializable;
import java.util.*;

public class GameMap implements Serializable {

    public static class Flags implements Serializable {
        final boolean permLight; // should the level always be lit?
        final boolean outside;  // if outside, then we can illuminate based on day/night
        final boolean poisonGas;  // player loses health if not wearing gasmask
        final boolean hot;  // player loses health if not wearing heatsuit

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
    public final List<DungeonEffect> effects;
    private List<LightSource> lightSources;
    public final Map<String, Point> keyLocations;  // useful for joining areas together
    public final String id; // internal id of area
    public final String name; // user visible name
    public final int level;  // difficulty level
    public ShopData shopData; // for stores contained in this map
    public final Flags flags;
    public boolean visited;  // has the player visited this map
    public boolean petCouldNotBePlaced;

    public void updatePlayerVisibilityData(Player player, Player pet) {
        updateBrightness(player, pet);
        updateSeenLocationsAndMonsters(player, pet);
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
        if (lightSource == null) {
            return;
        }
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
    private void updateBrightness(Player player, Player pet) {
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
        if (pet != null && this.actors.contains(pet)) {
            addBrightness(pet);
        }
    }

    public GameMap(String id,
                   String name,
                   int level,
                   DungeonSquare[][] map,
                   Map<String, Point> keyLocations,
                   MonsterIdGroup genMonsterIds,
                   Flags flags,
                   ShopData shopData) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.map = map;
        this.height = Array2D.height(this.map);
        this.width = Array2D.width(this.map);
        this.keyLocations = keyLocations;
        this.genMonsterIds = genMonsterIds;
        this.actors = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.flags = flags;
        this.shopData = shopData;
        this.visited = false;
        this.petCouldNotBePlaced = false;
        initLightSources();
    }

    // Call when a player enters a level.
    // Player is set to the requested location, and set to full energy.
    // Pet is moved as near to the player as possible.
    // Returns true of pet placement was successful.
    public boolean placePlayerAndPet(Player player, Point playerLoc, Player pet) {
        // Make sure the player doesn't appear on a monster.
        player.loc = findClosestOpenSquare(player, playerLoc);
        visited = true;
        addActor(player);
        player.energy.setFull(); // make sure the player can move first

        if (pet != null) {
            Point nearestLocation = findClosestOpenSquare(pet, player.loc);
            if (nearestLocation != null) {
                addActor(pet);
                pet.loc = nearestLocation;
                petCouldNotBePlaced = false;
            } else {
                petCouldNotBePlaced = true;
            }
        }

        updatePlayerVisibilityData(player, pet);
        return (!petCouldNotBePlaced);
    }

    // Returns true of pet placement was successful.
    public boolean placePet(Player player, Player pet) {
        if (pet != null) {
            Point nearestLocation = findClosestOpenSquare(pet, player.loc);
            if (nearestLocation != null) {
                addActor(pet);
                pet.loc = nearestLocation;
                petCouldNotBePlaced = false;
            } else {
                petCouldNotBePlaced = true;
            }
        }
        updatePlayerVisibilityData(player, pet);
        return (!petCouldNotBePlaced);
    }

    private void updateSeenLocationsAndMonsters(Player player, Player pet) {
        updateSeenLocationsAndMonsters(player);
        if (pet != null && this.actors.contains(pet)) {
            updateSeenLocationsAndMonsters(pet);
        }
    }

    private void updateSeenLocationsAndMonsters(Player player) {
        for (Point p : Circle.getPointsInCircle(player.viewRadius)) {
            int x = p.x + player.loc.x;
            int y = p.y + player.loc.y;
            if (!isOnMap(x,y)) continue;

            if (map[x][y].brightness > 0) {
                map[x][y].seen = true;

                Actor a = actorAt(x, y);
                if (a != null && !player.party.containsActor(a)) {
                    player.party.knowledge.addMonster(a);
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

    public void addActor(Actor a) {
        actors.add(a);
    }

    public void removeActor(Actor a) {
        int idx = actors.indexOf(a);
        if (currActorIdx > idx) {
            currActorIdx--;
        }
        actors.remove(a);
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
        this.currActorIdx = 0;
    }

    public boolean isOnMap(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    // Sees if the actor can go on this terrain (subject to
    // no other actors being there).
    public boolean isTerrainBlocked(Actor actor, int x, int y) {
        if (!isOnMap(x,y)) return true;
        return (!actor.terrestrial || map[x][y].blockGround()) &&
                (!actor.aquatic || map[x][y].blockWater());
    }

    public boolean hasTrapAt(int x, int y) {
        if (!isOnMap(x,y)) return false;
        DungeonFeature feature = map[x][y].feature;
        if (feature == null) return false;
        return feature.flags.trap;
    }

    public boolean isBlocked(Actor actor, int x, int y) {
        if (!isOnMap(x,y)) return true;
        if (isTerrainBlocked(actor, x, y)) return true;
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

    // Finds squares that are possible for the actor to move to.
    // They must not only be passable by the actor, free of monsters.
    // but also contiguous to the point 'loc'.  If 'nearestOnly'
    // is set, then this will break after the first (and nearest)
    // square is found.
    public List<Point> findAccessibleSquares(Actor actor, Point loc, boolean nearestOnly) {

        List<Point> result = new ArrayList<>();

        // Squares we've seen already.  These are contiguous to the actor area, but
        // not good (e.g., because there's a monster already in the square).
        Set<Point> seen = new HashSet<>();

        // Squares that we know can't work, because they're off the map or
        // have incompatible terrain or blocking features.
        Set<Point> disallowed = new HashSet<>();

        Deque<Point> toCheck = new ArrayDeque<>();
        // Start with both the original square and the immediate surrounding ones.
        // This is to handle the case where the original square is not passable,
        // notably when trying to find the nearest square by a teleport.
        // Duplicate the point so that we don't return an instance of
        // an existing point that might be modified by accident.
        toCheck.add(new Point(loc.x, loc.y));
        for (Direction d : Direction.ALL) {
            Point p = loc.add(d);
            toCheck.add(p);
        }

        while (!toCheck.isEmpty()) {
            Point curr = toCheck.removeFirst();

            // already seen point, or know point is not allowed.
            if (seen.contains(curr) || disallowed.contains(curr)) {
                continue;
            }

            // found a good spot.
            if (!isBlocked(actor, curr.x, curr.y)) {
                result.add(curr);
                if (nearestOnly) {
                    return result;
                }
            }

            if (isTerrainBlocked(actor, curr.x, curr.y)) {
                disallowed.add(curr);
            } else {
                seen.add(curr);
                for (Direction d : Direction.ALL) {
                    Point p = curr.add(d);
                    toCheck.add(p);
                }
            }
        }

        return result;
    }

    // Finds the closest open square to the starting location.
    // Returns null if no place exists.
    public Point findClosestOpenSquare(Actor actor, Point start) {
        List<Point> accessible = findAccessibleSquares(actor, start, true);
        if (accessible.isEmpty()) {
            return null;
        }
        return accessible.get(0);
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
