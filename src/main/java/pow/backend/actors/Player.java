package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.LightSource;
import pow.util.Circle;
import pow.util.DieRoll;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Player extends Actor implements Serializable, LightSource {
    private Queue<Action> actionQueue;

    public int viewRadius;
    public int lightRadius;
    public List<DungeonItem> equipment;
    public int cStr;
    public int cDex;
    public int cInt;
    public int cCon;
    public int experience;
    public int level;

    private static final int[] levelBreakpoints = {
            0, // level 1
            10,
            25,
            47,
            80,
            130,
            205,
            318,
            488,
            744,
            1128,
            1704,
            2568,
            3865,
            5811,
            8730,
            13108,
            19676,
            29528,
            44306,
            66474,
            99726};

    public Player(DungeonObject.Params objectParams, int maxHealth,
                  int cStr, int cDex, int cInt, int cCon,
                  DieRoll attackDamage) {
        super(objectParams, new Actor.Params(maxHealth, cDex, cDex, attackDamage, true, 0));
        this.actionQueue = new LinkedList<>();
        this.viewRadius = 11;  // how far can you see, assuming things are lit
        this.lightRadius = 8;  // 3 = candle (starting), 8 = lantern, 13 = bright lantern
        this.equipment = new ArrayList<>();
        this.toHit = 0;
        this.cStr = cStr;
        this.cDex = cDex;
        this.cInt = cInt;
        this.cCon = cCon;
        this.experience = 0;
        this.level = 1;
    }

    public void addCommand(Action request) {
        this.actionQueue.add(request);
    }

    public boolean canSee(GameState gs, Point point) {
        // must be within the player's view radius, and must be lit
        return ((MathUtils.dist2(loc, point) <= Circle.getRadiusSquared(viewRadius)) &&
                (gs.world.currentMap.map[point.x][point.y].brightness > 0));
    }

    @Override
    public String getPronoun() {
        return "you";
    }

    @Override
    public boolean needsInput() {
        return actionQueue.isEmpty();
    }

    @Override
    public Action act(GameBackend backend) {
        return this.actionQueue.poll();
    }

    @Override
    public Point getLocation() { return this.loc; }

    @Override
    public int getLightRadius() {
        return this.lightRadius;
    }

    // update our stats to include current equipped items and other bonuses.
    private void updateStats() {
        defense = cDex;
        for (DungeonItem item : equipment) {
            defense += item.defense + item.bonuses[DungeonItem.DEF_IDX];
        }

        int innateToHit = cDex;
        int innateToDam = cStr;
        attackDamage = new DieRoll(0, 0, innateToDam);
        for (DungeonItem item : equipment) {
            if (item.slot == DungeonItem.Slot.WEAPON) {
                attackDamage = new DieRoll(item.attack.roll, item.attack.die, item.attack.plus + innateToDam);
            }
        }
        toHit = innateToHit;
        for (DungeonItem item : equipment) {
            attackDamage.plus += item.bonuses[DungeonItem.TO_DAM_IDX];
            toHit += item.bonuses[DungeonItem.TO_HIT_IDX];
        }
    }

    // returns the old item, if any
    public DungeonItem wear(DungeonItem item) {
        DungeonItem.Slot slot = item.slot;
        for (int i = 0; i < equipment.size(); i++) {
            if (equipment.get(i).slot == slot) {
                // replace old item
                DungeonItem oldItem = equipment.remove(i);
                equipment.add(item);
                updateStats();
                return oldItem;
            }
        }
        // no item to replace
        equipment.add(item);
        updateStats();
        return null;
    }

    public DungeonItem takeOff(int idx) {
        DungeonItem item = equipment.remove(idx);
        updateStats();
        return item;
    }
}
