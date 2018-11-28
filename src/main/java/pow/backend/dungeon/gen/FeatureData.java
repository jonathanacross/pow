package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.utils.ParseUtils;
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

    private static final FeatureData instance;
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
        ActionParams actionParams = ParseUtils.parseActionParams(line[4]);

        return new DungeonFeature(id, name, image, flags, actionParams);
    }

    private DungeonFeature.Flags parseFlags(String text) {
        String[] tokens = text.split(",", -1);

        boolean actOnStep = false;
        boolean blockGround = false;
        boolean blockWater = false;
        boolean blockAir = false;
        boolean glowing = false;
        boolean stairsUp = false;
        boolean stairsDown = false;
        boolean trap = false;
        boolean pearlTile = false;
        boolean openDoor = false;
        boolean interesting = false;

        for (String t : tokens) {
            switch (t) {
                // TODO: fix names to agree: stairsDown, stairsUp, glowing
                case "": break;  // will happen if we have an empty string
                case "actOnStep": actOnStep = true; break;
                case "blockAir": blockAir = true; break;
                case "blockGround": blockGround = true; break;
                case "blockLava": break;
                case "blockWater": blockWater = true; break;
                case "closedDoor": break;
                case "interesting": interesting = true; break;
                case "downstairs": stairsDown = true; break;
                case "trap": trap = true; break;
                case "pearlTile": pearlTile = true; break;
                case "openDoor": openDoor = true; break;
                case "smallLight": glowing = true; break;
                case "upstairs": stairsUp = true; break;
                default:
                    throw new IllegalArgumentException("unknown feature flag '" + t + "'");
            }
        }

        return new DungeonFeature.Flags(
                blockGround, blockWater, blockAir, glowing, actOnStep,
                stairsUp, stairsDown, trap, pearlTile, openDoor, interesting);
    }
}
