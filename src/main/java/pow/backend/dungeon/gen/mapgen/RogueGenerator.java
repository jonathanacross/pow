package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.*;
import pow.util.Array2D;
import pow.util.MathUtils;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RogueGenerator implements MapGenerator {

    private int vaultLevel;
    private int width;
    private int height;
    private int level;
    private ProtoTranslator translator;
    private List<String> monsterIds;
    private List<String> vaultIds;
    private List<String> greatVaultIds;

    public RogueGenerator(int width, int height, int vaultLevel, int level, ProtoTranslator translator, List<String> monsterIds) {
        this.width = width;
        this.height = height;
        this.vaultLevel = vaultLevel;
        this.level = level;
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.vaultIds = new ArrayList<>(PremadeMapData.getRoomIds());
        this.greatVaultIds = new ArrayList<>(PremadeMapData.getVaultIds());
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
        int numItems = (width - 1) * (height - 1) / 100;
        GeneratorUtils.addItems(level, dungeonSquares, numItems, rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, this.monsterIds, null);
        return map;
    }

    private RoomInfo genRoom(int vaultLevel, int width, int height, Random rng) {
        int minRadius = 3;
        int maxRadius = 8;

        if (vaultLevel > 0) {
            // pick a vault
            PremadeMapData.PremadeMapInfo vault = (vaultLevel == 1) ?
                PremadeMapData.getRoom(vaultIds.get(rng.nextInt(vaultIds.size()))) :
                PremadeMapData.getVault(greatVaultIds.get(rng.nextInt(greatVaultIds.size())));
            int vaultWidth = Array2D.width(vault.data) - 2; // -2 since removing # border
            int vaultHeight =  Array2D.height(vault.data) - 2;
            double xRadius = (double) vaultWidth / 2;
            double yRadius = (double) vaultHeight / 2;
            int x = rng.nextInt((int) Math.ceil(width - vaultWidth - 2)) + (int) Math.ceil(xRadius) + 1;
            int y = rng.nextInt((int) Math.ceil(height - vaultHeight - 2)) + (int) Math.ceil(yRadius) + 1;
            return new RoomInfo(xRadius, yRadius, x, y, vault);
        } else {
            // make an ordinary room
            int xRadius = rng.nextInt(maxRadius - minRadius) + minRadius;
            int yRadius = rng.nextInt(maxRadius - minRadius) + minRadius;
            int x = rng.nextInt(width - 2 * (xRadius + 1)) + xRadius + 1;
            int y = rng.nextInt(height - 2 * (yRadius + 1)) + yRadius + 1;
            return new RoomInfo((double) xRadius, (double) yRadius, x, y, null);
        }

    }

    private boolean roomOverlaps(RoomInfo room, List<RoomInfo> roomList) {
        for (RoomInfo prevRoom : roomList) {
            int dx = Math.abs(room.x - prevRoom.x);
            int dy = Math.abs(room.y - prevRoom.y);
            double minDistX = room.xRadius + prevRoom.xRadius + 1;
            double minDistY = room.yRadius + prevRoom.yRadius + 1;
            if (dx <= minDistX && dy <= minDistY) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> visitPermutation(List<RoomInfo> rooms) {
        List<Integer> permutation = new ArrayList<>();

        boolean[] seen = new boolean[rooms.size()];

        // start with first room
        int currRoom = 0;
        seen[currRoom] = true;
        permutation.add(currRoom);

        while (permutation.size() < rooms.size()) {
            // find nearest unseen room
            int bestDist = 10000;
            int closestRoom = -1;
            for (int i = 0; i < rooms.size(); i++) {
                if (!seen[i]) {
                    int dist = Math.abs(rooms.get(i).x - rooms.get(currRoom).x) +
                               Math.abs(rooms.get(i).y - rooms.get(currRoom).y);
                    if (dist < bestDist) {
                        bestDist = dist;
                        closestRoom = i;
                    }
                }
            }

            currRoom = closestRoom;
            seen[currRoom] = true;
            permutation.add(currRoom);
        }

        return permutation;
    }

    private boolean isInRoom(List<RoomInfo> rooms, int x, int y) {
        for (RoomInfo room: rooms) {
            if ((x >= room.xlo) && (x < room.xhi) && (y >= room.ylo) && (y < room.yhi)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnteringRoom(List<RoomInfo> rooms, int x, int y, int dx, int dy) {
        return (!isInRoom(rooms, x, y) && isInRoom(rooms, x + dx, y + dy));
    }

    private boolean isLeavingRoom(List<RoomInfo> rooms, int x, int y, int dx, int dy) {
        return (isInRoom(rooms, x - dx, y - dy) && !isInRoom(rooms, x, y));
    }

    private boolean isEnteringOrLeavingRoom(List<RoomInfo> rooms, int x, int y, int dx, int dy) {
        return (isEnteringRoom(rooms, x, y, dx, dy) || isLeavingRoom(rooms, x, y, dx, dy));
    }

    private int numAdjWalls(int[][] data, int x, int y) {
        int numAdjWalls = 0;
        if (data[x][y-1] == Constants.TERRAIN_WALL) numAdjWalls++;
        if (data[x][y+1] == Constants.TERRAIN_WALL) numAdjWalls++;
        if (data[x-1][y] == Constants.TERRAIN_WALL) numAdjWalls++;
        if (data[x+1][y] == Constants.TERRAIN_WALL) numAdjWalls++;
        return numAdjWalls;
    }

    // detects if we are on the edge of a room, moving along
    // the edge
    // e.g.,  * = current position, > indicates direction
    // ##########
    // ####*>####
    // ###....###
    // ###....###
    // ###....###
    // ##########
    private boolean isMovingAlongsideRoom(List<RoomInfo> rooms, int x, int y, int dx, int dy) {
        if (dx == 0) {
            return (!isInRoom(rooms, x, y) && (isInRoom(rooms, x + 1, y) || isInRoom(rooms, x - 1, y)));
        } else {
            return (!isInRoom(rooms, x, y) && (isInRoom(rooms, x, y - 1) || isInRoom(rooms, x, y + 1)));
        }
    }

    // adds a new path
    private void addPath(int[][] data, List<RoomInfo> rooms, int oldx, int oldy, int newx, int newy) {
        int dx = MathUtils.sign(newx - oldx);
        int dy = MathUtils.sign(newy - oldy);

        for (int x = oldx; x != newx; x += dx) {
            if (!isMovingAlongsideRoom(rooms, x, oldy, dx, 0)) {
                data[x][oldy] = Constants.TERRAIN_FLOOR;
            }
            if (isEnteringOrLeavingRoom(rooms, x, oldy, dx, 0)) {
                data[x][oldy] = Constants.TERRAIN_FLOOR + Constants.FEATURE_CLOSED_DOOR;
            }
        }
        for (int y = oldy; y != newy; y += dy) {
            if (!isMovingAlongsideRoom(rooms, newx, y, 0, dy)) {
                data[newx][y] = Constants.TERRAIN_FLOOR;
            }
            if (isEnteringOrLeavingRoom(rooms, newx, y, 0, dy)) {
                data[newx][y] = Constants.TERRAIN_FLOOR + Constants.FEATURE_CLOSED_DOOR;
            }
        }

        // correct for the special case where we create a door
        // with only one exit (happens in some cases when the bend
        // of the path is on the edge of a room).
        if (numAdjWalls(data, newx, oldy) > 2) {
            data[newx][oldy] = Constants.TERRAIN_WALL;
        }
    }

    private int[][] genMap(int width, int height, Random rng) {

        // initialize to solid wall
        int[][] data = GeneratorUtils.solidMap(width, height);

        // pick rooms until we get some percent of the area
        List<RoomInfo> rooms = new ArrayList<>();
        double area = 0.0;
        if (vaultLevel == 2) {
            RoomInfo room = genRoom(vaultLevel, width, height, rng);
            rooms.add(room);
        }
        while (area < 0.25 * width * height) {
            RoomInfo room;
            if (vaultLevel >= 1 && rng.nextInt(5) == 0) {
                room = genRoom(1, width, height, rng);
            } else {
                room = genRoom(0, width, height, rng);
            }
            if (!roomOverlaps(room, rooms)) {
                rooms.add(room);
                area += (room.xRadius * 2 + 1) * (room.yRadius * 2 + 1);
            }
        }

        // clear out the area in the rooms
        for (RoomInfo room : rooms) {
            for (int x = room.xlo; x < room.xhi; x++) {
                for (int y = room.ylo; y < room.yhi; y++) {
                    data[x][y] = Constants.TERRAIN_FLOOR;
                }
            }
        }

        // sort the rooms by closest path so there aren't
        // too many long paths that criss-cross over the
        // dungeon..
        List<Integer> pi = visitPermutation(rooms);
        // but add a few extra paths so that the final result isn't
        // too circular.
        pi.add(pi.get(0));
        pi.add(pi.get(rooms.size() / 4));
        pi.add(pi.get(2 * rooms.size() / 4));
        pi.add(pi.get(3 * rooms.size() / 4));

        // connect the rooms with corridors -- just go in order
        for (int i = 1; i < pi.size(); i++) {
            int oldx = MathUtils.roundEven(rooms.get(pi.get(i - 1)).x);
            int oldy = MathUtils.roundEven(rooms.get(pi.get(i - 1)).y);
            int newx = MathUtils.roundEven(rooms.get(pi.get(i)).x);
            int newy = MathUtils.roundEven(rooms.get(pi.get(i)).y);

            addPath(data, rooms, oldx, oldy, newx, newy);
        }

        // fill in appropriate data from vaults
        for (RoomInfo room: rooms) {
            if (room.vault != null) {
                int xlo = room.xlo;
                int xhi = room.xhi;
                int ylo = room.ylo;
                int yhi = room.yhi;

                for (int x = xlo; x < xhi; x++) {
                    for (int y = ylo; y < yhi; y++) {
                        data[x][y] = room.vault.data[x - xlo + 1][y - ylo + 1];
                    }
                }
            }
        }

        return data;
    }

    private static class RoomInfo {
        double xRadius;
        double yRadius;
        int x;
        int y;
        PremadeMapData.PremadeMapInfo vault; // make a VaultClass?

        int xlo;
        int xhi;
        int ylo;
        int yhi;

        public RoomInfo(double xRadius, double yRadius, int x, int y, PremadeMapData.PremadeMapInfo vault) {
            this.xRadius = xRadius;
            this.yRadius = yRadius;
            this.x = x;
            this.y = y;
            this.vault = vault;
            this.xlo = (int) Math.floor(x - xRadius);
            this.xhi = (int) Math.floor(x + xRadius);
            this.ylo = (int) Math.floor(y - yRadius);
            this.yhi = (int) Math.floor(y + yRadius);
        }
    }
}
