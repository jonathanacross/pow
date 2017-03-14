package pow.backend.dungeon.gen;

import pow.util.BlockReader;
import pow.util.DebugLogger;
import pow.util.Point;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PremadeMapData {
    public static class PremadeMapInfo {
        public final String name;
        public final int[][] data;
        public final Point upExitLoc;
        public final Point downExitLoc;

        public PremadeMapInfo(String name, int[][] data, Point upExitLoc, Point downExitLoc) {
            this.name = name;
            this.data = data;
            this.upExitLoc = upExitLoc;
            this.downExitLoc = downExitLoc;
        }
    }

    public static Set<String> getRoomIds() { return instance.roomMap.keySet(); }
    public static Set<String> getVaultIds() { return instance.vaultMap.keySet(); }
    public static Set<String> getLevelIds() { return instance.levelMap.keySet(); }

    public static PremadeMapInfo getRoom(String id) {
        if (instance.roomMap.containsKey(id)) {
            return instance.roomMap.get(id);
        } else {
            throw new IllegalArgumentException("Error: tried to get room for nonexistent id " + id);
        }
    }
    public static PremadeMapInfo getVault(String id) {
        if (instance.vaultMap.containsKey(id)) {
            return instance.vaultMap.get(id);
        } else {
            throw new IllegalArgumentException("Error: tried to get vault for nonexistent id " + id);
        }
    }
    public static PremadeMapInfo getLevel(String id) {
        if (instance.levelMap.containsKey(id)) {
            return instance.levelMap.get(id);
        } else {
            throw new IllegalArgumentException("Error: tried to get level for nonexistent id " + id);
        }
    }

    private static final PremadeMapData instance;
    private Map<String, PremadeMapInfo> roomMap;
    private Map<String, PremadeMapInfo> vaultMap;
    private Map<String, PremadeMapInfo> levelMap;

    static {
        try {
            instance = new PremadeMapData();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private PremadeMapData() throws IOException {
        this.roomMap = readMapFile("/data/rooms.txt");
        this.vaultMap = readMapFile("/data/vaults.txt");
        this.levelMap = readMapFile("/data/maps.txt");  // TODO: rename for consistency
    }

    private Map<String, PremadeMapInfo> readMapFile(String resourceName) throws IOException {
        InputStream roomStream = this.getClass().getResourceAsStream(resourceName);
        BlockReader roomReader = new BlockReader(roomStream);

        Map<String, PremadeMapInfo> roomMap = new HashMap<>();
        for (List<String> block : roomReader.getData()) {
            PremadeMapInfo premadeMapInfo = parseMapInfo(block);
            roomMap.put(premadeMapInfo.name, premadeMapInfo);
        }
        return roomMap;
    }

    public static PremadeMapInfo parseMapInfo(List<String> lines) {
        assert(lines.size() >= 2);

        String name = lines.get(0);
        int width = lines.get(1).length();
        int height = lines.size() - 1;
        boolean seenDownStairs = false;
        boolean seenUpStairs = false;
        Point downExitLoc = null;
        Point upExitLoc = null;

        int[][] data = new int[width][height];
        for (int row = 1; row < lines.size(); row++) {
            if (lines.get(row).length() != width) {
                throw new IllegalArgumentException("expected a line of length " + width + ".  Saw " + lines.get(row));
            }
            for (int col = 0; col < width; col++) {
                int x = col;
                int y = row - 1;
                data[x][y] = Constants.parseChar(lines.get(row).charAt(col));
                if (Constants.getFeature(data[x][y]) == Constants.FEATURE_DOWN_STAIRS) {
                    if (!seenDownStairs) {
                        seenDownStairs = true;
                        downExitLoc = new Point(x, y);
                    } else {
                        throw new IllegalArgumentException("found multiple >'s in map: " + name + ". At most one is allowed");
                    }
                }
                if (Constants.getFeature(data[x][y]) == Constants.FEATURE_UP_STAIRS) {
                    if (!seenUpStairs) {
                        seenUpStairs = true;
                        upExitLoc = new Point(x, y);
                    } else {
                        throw new IllegalArgumentException("found multiple <'s in map: " + name + ". At most one is allowed");
                    }
                }
            }
        }

        return new PremadeMapInfo(name, data, upExitLoc, downExitLoc);
    }
}
