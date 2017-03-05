package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonItem;

import java.util.HashMap;
import java.util.Map;

// various parsing utilities use for item/artifact/terrain generation
public class ParseUtils {

    // for item generation
    public static final int ATTACK_IDX = DungeonItem.TO_HIT_IDX;
    public static final int MATCH_BONUS = -99999;
    private static final Map<String, Integer> keyToBonusIdx;
    static {
        keyToBonusIdx = new HashMap<>();
        keyToBonusIdx.put("attack", ATTACK_IDX);
        keyToBonusIdx.put("def", DungeonItem.DEF_IDX);
        keyToBonusIdx.put("str", DungeonItem.STR_IDX);
        keyToBonusIdx.put("dex", DungeonItem.DEX_IDX);
        keyToBonusIdx.put("int", DungeonItem.INT_IDX);
        keyToBonusIdx.put("con", DungeonItem.CON_IDX);
        keyToBonusIdx.put("speed", DungeonItem.SPEED_IDX);
        keyToBonusIdx.put("wealth", DungeonItem.WEALTH_IDX);
    }

    public static int[] parseBonuses(String text) {
        int[] bonuses = new int[DungeonItem.NUM_BONUSES];
        if (text.isEmpty()) {
            return bonuses;
        }

        String[] statBonuses = text.split(",");
        for (String statBonus : statBonuses) {
            String[] tokens = statBonus.split(":", 2);
            // x indicates that we'll use the bounus calculated based on the level
            int bonusAmt = (tokens[1].charAt(0) == 'x') ? MATCH_BONUS : Integer.parseInt(tokens[1]);
            String stat = tokens[0];
            if (keyToBonusIdx.containsKey(stat)) {
                bonuses[keyToBonusIdx.get(stat)] = bonusAmt;
            } else {
                throw new RuntimeException("error: couldn't parse item stat bonus: " + stat);
            }
        }

        return bonuses;
    }

    public static DungeonItem.Flags parseFlags(String text) {
        String[] tokens = text.split(",", -1);

        boolean potion = false;
        boolean money = false;
        boolean arrow = false;
        for (String t : tokens) {
            switch (t) {
                case "": break;  // will happen if we have an empty string
                case "potion": potion = true; break;
                case "money": money = true; break;
                case "arrow": arrow = true; break;
                default: throw new IllegalArgumentException("unknown item flag '" + t + "'");
            }
        }

        return new DungeonItem.Flags(potion, money, arrow);
    }

    public static ActionParams parseActionParams(String text) {
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
