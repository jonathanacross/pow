package pow.backend.utils;

import pow.backend.ActionParams;
import pow.backend.dungeon.DungeonItem;
import pow.util.TextUtils;

import java.util.function.Function;

// class to make it easy to log (and read) buying stats of items
public class ItemCostStats {

    public enum Attribute {
        TO_HIT(0, "toHit", item -> item.bonuses[DungeonItem.TO_HIT_IDX]),
        TO_DAM(1, "toDam", item -> item.bonuses[DungeonItem.TO_DAM_IDX]),
        DEF(2, "toDef", item -> item.bonuses[DungeonItem.DEF_IDX]),
        STR(3, "toStr", item -> item.bonuses[DungeonItem.STR_IDX]),
        DEX(4, "toDex", item -> item.bonuses[DungeonItem.DEX_IDX]),
        INT(5, "toInt", item -> item.bonuses[DungeonItem.INT_IDX]),
        CON(6, "toCon", item -> item.bonuses[DungeonItem.CON_IDX]),
        RES_FIRE(7, "rFire", item -> item.bonuses[DungeonItem.RES_FIRE_IDX]),
        RES_COLD(8, "rCold", item -> item.bonuses[DungeonItem.RES_COLD_IDX]),
        RES_ACID(9, "rAcid", item -> item.bonuses[DungeonItem.RES_ACID_IDX]),
        RES_ELEC(10, "rElec", item -> item.bonuses[DungeonItem.RES_ELEC_IDX]),
        RES_POIS(11, "rPois", item -> item.bonuses[DungeonItem.RES_POIS_IDX]),
        RES_DAM(12, "rDam", item -> item.bonuses[DungeonItem.RES_DAM_IDX]),
        SPEED(13, "speed", item -> item.bonuses[DungeonItem.SPEED_IDX]),
        WEALTH(14, "wealth", item -> item.bonuses[DungeonItem.WEALTH_IDX]),
        SOCKETS(15, "sockets", item -> item.bonuses[DungeonItem.SOCKETS_IDX]),
        ACT_SPEED(16, "actSpeed", item -> item.actionParams.actionName == ActionParams.ActionName.MODIFY_SPEED_ACTION ? item.actionParams.number : 0),
        ACT_HEAL(17, "actHeal", item -> item.actionParams.actionName == ActionParams.ActionName.HEAL_ACTION ? item.actionParams.number : 0),
        ACT_MANA(18, "actMana", item -> item.actionParams.actionName == ActionParams.ActionName.RESTORE_MANA_ACTION ? item.actionParams.number : 0),
        ACT_RESTORE(19, "actRestore", item -> item.actionParams.actionName == ActionParams.ActionName.RESTORE_ACTION ? item.actionParams.number : 0),
        ACT_HERO(20, "actHero", item -> item.actionParams.actionName == ActionParams.ActionName.HEROISM_ACTION ? item.actionParams.number : 0),
        ACT_AGIL(21, "actAgil", item -> item.actionParams.actionName == ActionParams.ActionName.AGILITY_ACTION ? item.actionParams.number : 0),
        ARROW(22, "arrow", item -> item.flags.arrow ? 1 : 0);

        private int index;
        private String name;
        private Function<DungeonItem, Integer> extractFn;

        Attribute(int index, String name, Function<DungeonItem, Integer> extractFn) {
            this.index = index;
            this.name = name;
            this.extractFn = extractFn;
        }

        public int getIndex() { return index;}
        public String getName() {return name; }
        public int getValue(DungeonItem item) { return extractFn.apply(item); }
    }

    public enum Action {
        BUY,
        SEE
    }

    public final String name;
    public final int[] attributes;
    public final DungeonItem.Slot slot;
    public final Action action;
    public final int cost;

    public ItemCostStats(DungeonItem item, Action action, int cost) {
        this.name = TextUtils.format(item.name, 1, false) + " " + item.bonusString();
        this.slot = item.slot;
        this.attributes = new int[Attribute.values().length];
        for (Attribute a : Attribute.values()) {
            this.attributes[a.getIndex()] = a.getValue(item);
        }
        this.action = action;
        this.cost = cost;
    }

    public String toTsv() {
        StringBuilder sb = new StringBuilder(name + "\t" + action.toString() + "\t" + cost + "\t" + slot);
        for (int a : attributes) {
            sb.append("\t" + a);
        }
        sb.append("\n");
        return sb.toString();
    }

    // from TSV
    public ItemCostStats(String[] fields) {
        this.name = fields[0];
        this.action = Action.valueOf(fields[1]);
        this.cost = Integer.parseInt(fields[2]);
        this.slot = DungeonItem.Slot.valueOf(fields[3]);
        this.attributes = new int[Attribute.values().length];
        for (int i = 0; i < Attribute.values().length; i++) {
            this.attributes[i] = Integer.parseInt(fields[4 + i]);
        }
    }
}
