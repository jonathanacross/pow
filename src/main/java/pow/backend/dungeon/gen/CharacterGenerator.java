package pow.backend.dungeon.gen;

import pow.backend.SpellParams;
import pow.backend.actors.Abilities;
import pow.backend.actors.GainRatios;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.util.DebugLogger;
import pow.util.Point;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CharacterGenerator {

    public static class CharacterData {
        public final String id;
        public final boolean isPet;
        public final String name;
        public final String image;
        public final String description;
        public final double strGain;
        public final double dexGain;
        public final double intGain;
        public final double conGain;
        public final double speedGain;
        public final Abilities abilities;
        public final List<DungeonItem> startItems;
        public final List<SpellParams> spells;

        public CharacterData(String id,
                             boolean isPet,
                             String name,
                             String image,
                             String description,
                             double strGain,
                             double dexGain,
                             double intGain,
                             double conGain,
                             double speedGain,
                             Abilities abilities,
                             List<DungeonItem> startItems,
                             List<SpellParams> spells) {
            this.id = id;
            this.isPet = isPet;
            this.name = name;
            this.image = image;
            this.description = description;
            this.strGain = strGain;
            this.dexGain = dexGain;
            this.intGain = intGain;
            this.conGain = conGain;
            this.speedGain = speedGain;
            this.abilities = abilities;
            this.startItems = startItems;
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

    public static List<CharacterData> getPlayerCharacterData() {
        List<CharacterData> players = new ArrayList<>();
        for (CharacterData cd : instance.characterData) {
            if (!cd.isPet) {
                players.add(cd);
            }
        }
        return players;
    }

    public static List<CharacterData> getPetCharacterData() {
        List<CharacterData> players = new ArrayList<>();
        for (CharacterData cd : instance.characterData) {
            if (cd.isPet) {
                players.add(cd);
            }
        }
        return players;
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
                characterData.intGain, characterData.conGain, characterData.speedGain);

        return new Player(objectParams, gainRatios, characterData.spells, characterData.abilities, characterData.startItems);
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

    private static Abilities parseAbilities(String text) {
        String[] tokens = text.split(",", -1);
        boolean archeryBonus = false;
        boolean poisonDamage = false;
        boolean stunDamage = false;

        for (String t : tokens) {
            if (t.isEmpty()) continue;
            if (t.equals("archeryBonus")) { archeryBonus = true; }
            if (t.equals("poisonDamage")) { poisonDamage = true; }
            if (t.equals("stunDamage")) { stunDamage = true; }
        }

        return new Abilities(archeryBonus, poisonDamage, stunDamage);
    }

    private static List<DungeonItem> parseStartItems(String text) {
        List<DungeonItem> items = new ArrayList<>();
        String[] tokens = text.split(",", -1);
        Random rng = new Random(123); // use fixed generator; will always have the same..
        for (String itemId : tokens) {
            if (itemId.isEmpty()) continue;

            items.add(ItemGenerator.genItem(itemId, 1, rng));
        }

        return items;
    }

    private static CharacterData parseCharacter(String[] line) {
        String id;
        boolean isPet;
        String name;
        String image;
        String description;
        double strGain;
        double dexGain;
        double intGain;
        double conGain;
        double speedGain;
        List<DungeonItem> startItems;
        List<SpellParams> spells;
        Abilities abilities;

        if (line.length != 13) {
            throw new IllegalArgumentException("Expected 13 fields, but had " + line.length
                    + ". Fields = \n" + String.join(",", line));
        }

        try {
            id = line[0];
            isPet = line[1].equals("pet");
            name = line[2];
            image = line[3];
            description = line[4];
            strGain = Double.parseDouble(line[5]);
            dexGain = Double.parseDouble(line[6]);
            intGain = Double.parseDouble(line[7]);
            conGain = Double.parseDouble(line[8]);
            speedGain = Double.parseDouble(line[9]);
            abilities = parseAbilities(line[10]);
            startItems = parseStartItems(line[11]);
            spells = parseSpells(line[12]);

            return new CharacterData(id, isPet, name, image, description,
                    strGain, dexGain, intGain, conGain, speedGain,
                    abilities, startItems, spells);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage() + "\nFields = \n" +
                    String.join(",", line), e);
        }
    }
}
