package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonTerrain;
import pow.util.DebugLogger;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TerrainData {

    public static Set<String> getIds() {
        return instance.terrainMap.keySet();
    }

    public static TerrainData instance;
    private Map<String, DungeonTerrain> terrainMap;

    public static DungeonTerrain getTerrain(String id) { return instance.terrainMap.get(id); }

    static {
        try {
            instance = new TerrainData();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private TerrainData() throws IOException {
        //Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/terrain.txt");
        TsvReader reader = new TsvReader(tsvStream);

        terrainMap = new HashMap<>();
        for (String[] line : reader.getData()) {
            DungeonTerrain terrain = parseTerrain(line);
            terrainMap.put(terrain.id, terrain);
        }
    }

    // Parses the generator from text.
    // For now, assumes TSV, but may change this later.
    private DungeonTerrain parseTerrain(String[] line) {
        if (line.length != 5) {
            throw new IllegalArgumentException("Expected 5 fields, but had " + line.length

            + ". Fields = \n" + String.join(",", line));
        }

        String id = line[0];
        String name = line[1];
        String image = line[2];
        DungeonTerrain.Flags flags = parseFlags(line[3]);
        ActionParams actionParams = parseActionParams(line[4]);

        return new DungeonTerrain(id, name, image, flags, actionParams);
    }

    private ActionParams parseActionParams(String text) {
        ActionParams params = new ActionParams();
        if (text.isEmpty()) {
            return params;
        }
        String[] tokens = text.split(":", 3);

        params.actionName = tokens[0];

        if (!tokens[1].isEmpty()) {
            params.number = Integer.parseInt(tokens[1]);
        }

        if (!tokens[2].isEmpty()) {
            params.name = tokens[2];
        }

        return params;
    }

    private DungeonTerrain.Flags parseFlags(String text) {
        String[] tokens = text.split(",", -1);

        boolean blockGround = false;
        boolean diggable = false;
        boolean actOnStep = false;
        boolean teleport = false;

        for (String t : tokens) {
            switch (t) {
                case "": break;  // will happen if we have an empty string
                case "actOnStep": actOnStep = true; break;
                case "blockAir": break;
                case "blockGround": blockGround = true; break;
                case "blockLava": break;
                case "blockWater": break;
                case "diggable": diggable = true; break;
                case "teleport": teleport = true;
                default:
                    throw new IllegalArgumentException("unknown terrain flag '" + t + "'");
            }
        }

        return new DungeonTerrain.Flags(blockGround, diggable, actOnStep, teleport);
    }
}
