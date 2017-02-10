package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonFeature;
import pow.util.DebugLogger;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatureData {

    public static Set<String> getIds() {
        return instance.featureMap.keySet();
    }

    public static DungeonFeature getFeature(String id) {
        if (instance.featureMap.containsKey(id)) {
            return instance.featureMap.get(id);
        } else {
            System.out.println("error: can't find feature with id '" + id + "'");
            return instance.featureMap.get("debug");
        }
    }

    private static FeatureData instance;
    private Map<String, DungeonFeature> featureMap;

    static {
        try {
            instance = new FeatureData();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private FeatureData() throws IOException {
        //Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/features.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        featureMap = new HashMap<>();
        for (String[] line : reader.getData()) {
            DungeonFeature feature = parseFeature(line);
            featureMap.put(feature.id, feature);
        }
    }

    // Parses the generator from text.
    // For now, assumes TSV, but may change this later.
    private DungeonFeature parseFeature(String[] line) {
        if (line.length != 5) {
            throw new IllegalArgumentException("Expected 5 fields, but had " + line.length
            + ". Fields = \n" + String.join(",", line));
        }

        String id = line[0];
        String name = line[1];
        String image = line[2];
        DungeonFeature.Flags flags = parseFlags(line[3]);
        ActionParams actionParams = parseActionParams(line[4]);

        return new DungeonFeature(id, name, image, flags, actionParams);
    }

    private DungeonFeature.Flags parseFlags(String text) {
        String[] tokens = text.split(",", -1);

        boolean actOnStep = false;
        boolean blockGround = false;
        boolean blockAir = false;
        boolean glowing = false;
        boolean stairsUp = false;
        boolean stairsDown = false;
        boolean openDoor = false;

        for (String t : tokens) {
            switch (t) {
                // TODO: fix names to agree: stairsDown, stairsUp, glowing
                case "": break;  // will happen if we have an empty string
                case "actOnStep": actOnStep = true; break;
                case "blockAir": blockAir = true; break;
                case "blockGround": blockGround = true; break;
                case "blockLava": break;
                case "blockWater": break;
                case "closedDoor": break;
                case "downstairs": stairsDown = true; break;
                case "openDoor": openDoor = true; break;
                case "smallLight": glowing = true; break;
                case "upstairs": stairsUp = true; break;
                default:
                    throw new IllegalArgumentException("unknown feature flag '" + t + "'");
            }
        }

        return new DungeonFeature.Flags(blockGround, blockAir, glowing, actOnStep, stairsUp, stairsDown, openDoor);
    }

    // TODO: duplicate code in TerrainData, ItemGenerator
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
}
