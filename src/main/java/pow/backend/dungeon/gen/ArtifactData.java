package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonItem;
import pow.util.DebugLogger;
import pow.util.DieRoll;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ArtifactData {

    public static Set<String> getArtifactIds() {
        return instance.artifacts.keySet();
    }

    // generates a single item
    public static DungeonItem getArtifact(String id) {
        if (!instance.artifacts.containsKey(id)) {
            DebugLogger.error("unknown artifact id '" + id + "'");
        }
        return instance.artifacts.get(id);
    }

    private static ArtifactData instance;
    private Map<String, DungeonItem> artifacts;

    static {
        try {
            instance = new ArtifactData();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private ArtifactData() throws IOException {
        // Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/artifacts.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        artifacts = new HashMap<>();
        for (String[] line : reader.getData()) {
            DungeonItem item = parseArtifact(line);
            artifacts.put(item.id, item);
        }
    }

    private static DungeonItem parseArtifact(String[] line) {
        String id;
        String name;
        String image;
        String description;
        DungeonItem.Slot slot;
        DungeonItem.Flags flags;
        ActionParams actionParams;
        int[] bonuses;
        DieRoll attack;
        int defense;
        String extra;

        if (line.length != 11) {
            throw new IllegalArgumentException("Expected 11 fields, but had " + line.length
                    + ". Fields = \n" + String.join(",", line));
        }

        try {
            id = line[0];
            name = line[1];
            image = line[2];
            description = line[3];
            slot = DungeonItem.Slot.valueOf(line[4].toUpperCase());
            flags = ParseUtils.parseFlags(line[5]);
            actionParams = ParseUtils.parseActionParams(line[6]);
            bonuses = ParseUtils.parseBonuses(line[7]);
            attack = DieRoll.parseDieRoll(line[8]);
            defense = Integer.parseInt(line[9]);
            extra = line[10];

            return new DungeonItem(id, name, image, description, slot, flags, bonuses, attack,
                    defense, 1, actionParams);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage() + "\nFields = \n" + String.join(",", line), e);
        }
    }
}
