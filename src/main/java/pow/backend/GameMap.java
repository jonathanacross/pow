package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Pet;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.backend.actors.Monster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMap implements Serializable {
    public DungeonSquare[][] map; // indexed by x,y, or c,r
    public int width;
    public int height;
    public List<Actor> actors;

    public GameMap(Random rng, Player player, Pet pet) {
//        map = buildTestArea();
        map = buildArena(40, 30, rng);
        int x = width / 2;
        int y = height / 2;
        player.x = x;
        player.y = y;
        player.energy.setFull(); // make sure the player can move first
        actors.add(player);
        if (pet != null) {
            pet.x = x + 2;
            pet.y = y + 2;
            actors.add(pet);
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
            if (a.x == x && a.y == y && a.solid) return true;
        }
        return false;
    }

    public Actor actorAt(int x, int y) {
        for (Actor a: this.actors) {
            if (a.x == x && a.y == y && a.solid) return a;
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
                        new DungeonFeature.Flags(false));
        dungeonMap[(int) (width * 0.75)][(int) (height * 0.6)].feature =
                new DungeonFeature("losetile", "death", "cobra",
                        new DungeonFeature.Flags(false));

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
