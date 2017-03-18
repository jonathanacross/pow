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
            // x indicates that we'll use the bonus calculated based on the level
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

        switch (tokens[0]) {
            case "": params.actionName = ActionParams.ActionName.NO_ACTION; break;
            case "modifyTerrain": params.actionName = ActionParams.ActionName.MODIFY_TERRAIN_ACTION; break;
            case "modifyFeature": params.actionName = ActionParams.ActionName.MODIFY_FEATURE_ACTION; break;
            case "modifySpeed": params.actionName = ActionParams.ActionName.MODIFY_SPEED_ACTION; break;
            case "poison": params.actionName = ActionParams.ActionName.POISON_ACTION; break;
            case "moveToArea": params.actionName = ActionParams.ActionName.MOVE_TO_AREA_ACTION; break;
            case "heal": params.actionName = ActionParams.ActionName.HEAL_ACTION; break;
            case "restoreMana": params.actionName = ActionParams.ActionName.RESTORE_MANA_ACTION; break;
            case "restore": params.actionName = ActionParams.ActionName.RESTORE_ACTION; break;
            case "enterShop": params.actionName = ActionParams.ActionName.ENTER_SHOP_ACTION; break;
            case "heroism": params.actionName = ActionParams.ActionName.HEROISM_ACTION; break;
            case "agility": params.actionName = ActionParams.ActionName.AGILITY_ACTION; break;
            case "unlockDoor": params.actionName = ActionParams.ActionName.UNLOCK_DOOR_ACTION; break;
            default: throw new IllegalArgumentException("unknown action name " + tokens[0]);
        }

        if (!tokens[1].isEmpty()) {
            params.number = Integer.parseInt(tokens[1]);
        }

        if (!tokens[2].isEmpty()) {
            params.name = tokens[2];
        }

        return params;
    }
}
