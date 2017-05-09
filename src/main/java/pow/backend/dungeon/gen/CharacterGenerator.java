package pow.backend.dungeon.gen;

import pow.backend.SpellParams;
import pow.backend.actors.GainRatios;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonObject;
import pow.util.DebugLogger;
import pow.util.Point;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterGenerator {

    public static class CharacterData {
        public final String id;
        public final String name;
        public final String image;
        public final String description;
        public final double strGain;
        public final double dexGain;
        public final double intGain;
        public final double conGain;
        public final List<SpellParams> spells;

        public CharacterData(String id,
                             String name,
                             String image,
                             String description,
                             double strGain,
                             double dexGain,
                             double intGain,
                             double conGain,
                             List<SpellParams> spells) {
            this.id = id;
            this.name = name;
            this.image = image;
            this.description = description;
            this.strGain = strGain;
            this.dexGain = dexGain;
            this.intGain = intGain;
            this.conGain = conGain;
            this.spells = spells;
        }
    }

    private static final CharacterGenerator instance;
    private Map<String, CharacterData> charDataMap;
    private List<CharacterData> characterData;

    static {
        try {
            instance = new CharacterGenerator();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    public static List<CharacterData> getCharacterData() {
        return instance.characterData;
    }

    public static Player getPlayer(String name, String id) {
        if (!instance.charDataMap.containsKey(id)) {
            DebugLogger.error("unknown character id '" + id + "'");
            throw new RuntimeException("unknown character id '" + id + "'");
        }
        CharacterData characterData = instance.charDataMap.get(id);

        DungeonObject.Params objectParams = new DungeonObject.Params(
                "player", // id
                name,
                characterData.image,
                characterData.description,
                new Point(-1, -1), // location -- will be updated later
                true);
        GainRatios gainRatios = new GainRatios("", characterData.strGain, characterData.dexGain,
                characterData.intGain, characterData.conGain);

        return new Player(objectParams, gainRatios, characterData.spells);
    }

    private CharacterGenerator() throws IOException {
        // Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/characters.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        charDataMap = new HashMap<>();
        characterData = new ArrayList<>();
        for (String[] line : reader.getData()) {
            CharacterData character = parseCharacter(line);
            charDataMap.put(character.id, character);
            characterData.add(character);
        }
    }

    private static List<SpellParams> parseSpells(String text) {
        String[] tokens = text.split(",", -1);
        List<SpellParams> spellList = new ArrayList<>();

        for (String t : tokens) {
            if (t.isEmpty()) continue;
            SpellParams spell = SpellData.getSpell(t);
            spellList.add(spell);
        }

        // sort the spells so we don't have to update data files
        spellList.sort((SpellParams a, SpellParams b) -> {
            if (a.minLevel != b.minLevel) {
                return Integer.compare(a.minLevel, b.minLevel);
            }
            return a.name.compareTo(b.name);
        });

        return spellList;
    }

    private static CharacterData parseCharacter(String[] line) {
        String id;
        String name;
        String image;
        String description;
        double strGain;
        double dexGain;
        double intGain;
        double conGain;
        List<SpellParams> spells;

        if (line.length != 9) {
            throw new IllegalArgumentException("Expected 9 fields, but had " + line.length
                    + ". Fields = \n" + String.join(",", line));
        }

        try {
            id = line[0];
            name = line[1];
            image = line[2];
            description = line[3];
            strGain = Double.parseDouble(line[4]);
            dexGain = Double.parseDouble(line[5]);
            intGain = Double.parseDouble(line[6]);
            conGain = Double.parseDouble(line[7]);
            spells = parseSpells(line[8]);

            return new CharacterData(id, name, image, description, strGain, dexGain, intGain, conGain, spells);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage() + "\nFields = \n" +
                    String.join(",", line), e);
        }
    }
}
