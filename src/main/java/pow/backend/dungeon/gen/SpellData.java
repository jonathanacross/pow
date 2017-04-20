package pow.backend.dungeon.gen;

import pow.backend.SpellParams;
import pow.util.DebugLogger;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpellData {

    public static SpellParams getSpell(String id) {
        if (!instance.spells.containsKey(id)) {
            DebugLogger.error("unknown spell id '" + id + "'");
            throw new RuntimeException("unknown spell id '" + id + "'");
        }
        return instance.spells.get(id);
    }

    private static final SpellData instance;
    private Map<String, SpellParams> spells;

    static {
        try {
            instance = new SpellData();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private SpellData() throws IOException {
        // Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/spells.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        spells = new HashMap<>();
        for (String[] line : reader.getData()) {
            SpellParams item = parseSpell(line);
            spells.put(item.id, item);
        }
    }

    private static SpellParams parseSpell(String[] line) {
        String id;
        String name;
        String description;
        int minLevel; // min level for a character to cast this
        int requiredMana;
        SpellParams.SpellType spellType;
        SpellParams.Element element;
        SpellParams.PowerStat powerStat;
        int size;  // related to size of area affected by this spell (for area spells)
        int amtBase;
        int amtDelta;  // total value for this spell will be amtBase + amtDelta*level


        if (line.length != 11) {
            throw new IllegalArgumentException("Expected 11 fields, but had " + line.length
                    + ". Fields = \n" + String.join(",", line));
        }

        try {
            id = line[0];
            name = line[1];
            minLevel = Integer.parseInt(line[2]);
            requiredMana = Integer.parseInt(line[3]);
            spellType = SpellParams.SpellType.valueOf(line[4].toUpperCase().replace(" ", "_"));
            element = SpellParams.Element.valueOf(line[5].toUpperCase().replace(" ", "_"));
            size = Integer.parseInt(line[6]);
            amtBase = Integer.parseInt(line[7]);
            amtDelta = Integer.parseInt(line[8]);
            powerStat = SpellParams.PowerStat.valueOf(line[9].toUpperCase().replace(" ", "_"));
            description = line[10];

            return new SpellParams( id, name, description, minLevel, requiredMana,
                    spellType, element, powerStat, size, amtBase, amtDelta);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + "\nFields = \n" + String.join(",", line), e);
        }
    }
}
