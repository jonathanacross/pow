package pow.backend.actors;

import pow.backend.AttackData;
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

    public static class GainRatios implements Serializable {
        public double strRatio;
        public double dexRatio;
        public double intRatio;
        public double conRatio;

        public GainRatios(double strRatio, double dexRatio, double intRatio, double conRatio) {
            this.strRatio = strRatio;
            this.dexRatio = dexRatio;
            this.intRatio = intRatio;
            this.conRatio = conRatio;
        }
    }

    // TODO: see if this should be shared with items.
    public static class Stats implements Serializable {
        public int strength;
        public int dexterity;
        public int intelligence;
        public int constitution;
        public int defense;
        public int weaponToHit;
        public int weaponToDam;
        public int bowToHit;
        public int bowToDam;
        public int speed;

        public Stats() {
            this.strength = 0;
            this.dexterity = 0;
            this.intelligence = 0;
            this.constitution = 0;
            this.defense = 0;
            this.weaponToHit = 0;
            this.weaponToDam = 0;
            this.bowToHit = 0;
            this.bowToDam = 0;
            this.speed = 0;
        }
    }

    private Queue<Action> actionQueue;

    public int viewRadius;
    public int lightRadius;
    public List<DungeonItem> equipment;
    private GainRatios gainRatios;
    private Stats innateStats;
    public Stats currStats;

    private AttackData innateAttack;
    //public AttackData weaponAttack;  // part of Actor
    public AttackData bowAttack;
    public int experience;
    public int level;

    // computed as totals in MakePlayerExpLevels
    private static final int[] levelBreakpoints = {
            0,
            10,
            27,
            54,
            99,
            173,
            295,
            497,
            830,
            1379,
            2285,
            3781,
            6249,
            10321,
            17040,
            28126,
            46418,
            76600,
            126400,
            208570,
            344150 };

    // default player
    public Player() {
        this(
                new DungeonObject.Params(
                        "player", // id
                        "", // name
                        "human_adventurer", // image
                        "yourself", // description
                        new Point(-1, -1), // location -- will be updated later
                        true), // solid
                new GainRatios(1.0, 1.0, 1.0, 1.0),
                new AttackData(new DieRoll(2, 2), 0, 0)
        );
    }

    public Player(DungeonObject.Params objectParams,
                  GainRatios gainRatios,
                  AttackData innateAttack ) {
        super(objectParams, new Actor.Params(-1, -99, 0, null, true, 0));
        this.actionQueue = new LinkedList<>();
        this.viewRadius = 11;  // how far can you see, assuming things are lit
        this.lightRadius = 8;  // 3 = candle (starting), 8 = lantern, 13 = bright lantern
        this.equipment = new ArrayList<>();
        this.innateStats = new Stats();
        this.gainRatios = gainRatios;
        this.innateAttack = innateAttack;
        this.bowAttack = null;
        this.experience = 0;
        this.level = 1;
        this.currStats = new Stats();
        updateStats();  // updates current stats, defense, and attack, bowAttack
        this.health = this.maxHealth;
    }

    public void addCommand(Action request) {
        this.actionQueue.add(request);
    }

    public boolean canSee(GameState gs, Point point) {
        // must be on the map, within the player's view radius, and must be lit
        return (gs.getCurrentMap().isOnMap(point.x, point.y) &&
                (MathUtils.dist2(loc, point) <= Circle.getRadiusSquared(viewRadius)) &&
                (gs.getCurrentMap().map[point.x][point.y].brightness > 0));
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

    private static int getMaxHP(int con) {
        return (int) Math.round(0.7 * con * con + 1.6 * con + 9.7);
    }

    // update our stats, plus toHit, defense to include current equipped items and other bonuses.
    private void updateStats() {
        currStats.strength = innateStats.strength;
        currStats.dexterity = innateStats.dexterity;
        currStats.intelligence = innateStats.intelligence;
        currStats.constitution = innateStats.constitution;
        currStats.defense = innateStats.defense;
        currStats.weaponToHit = innateStats.weaponToHit;
        currStats.weaponToDam = innateStats.weaponToDam;
        currStats.bowToHit = innateStats.bowToHit;
        currStats.bowToDam = innateStats.bowToDam;
        currStats.speed = innateStats.speed;

        for (DungeonItem item : equipment) {
            currStats.strength += item.bonuses[DungeonItem.STR_IDX];
            currStats.dexterity += item.bonuses[DungeonItem.DEX_IDX];
            currStats.intelligence += item.bonuses[DungeonItem.INT_IDX];
            currStats.constitution += item.bonuses[DungeonItem.CON_IDX];
            currStats.defense += item.defense + item.bonuses[DungeonItem.DEF_IDX];
            currStats.speed += item.bonuses[DungeonItem.SPEED_IDX];
            // Only add non bow/weapon (i.e. from rings/amulets) for toHit/toDam bonuses.
            // The weapon and bow bonuses will be applied to the weapon and bow, separately, later.
            if (item.slot != DungeonItem.Slot.BOW && item.slot != DungeonItem.Slot.WEAPON) {
                currStats.weaponToHit += item.bonuses[DungeonItem.TO_HIT_IDX];
                currStats.weaponToDam += item.bonuses[DungeonItem.TO_DAM_IDX];
                currStats.bowToHit += item.bonuses[DungeonItem.TO_HIT_IDX];
                currStats.bowToDam += item.bonuses[DungeonItem.TO_DAM_IDX];
            }
        }

        // compute toHit, toDam, defense
        int overallWeaponToHit = currStats.weaponToHit + currStats.dexterity;
        int overallWeaponToDam = currStats.weaponToDam + currStats.strength;
        int overallBowToHit = currStats.bowToHit + currStats.dexterity;
        int overallBowToDam = currStats.bowToDam + currStats.strength;
        defense = currStats.defense + currStats.dexterity;
        speed = currStats.speed;
        maxHealth = getMaxHP(currStats.constitution);

        // Compute attack damage.
        // use innate attack by default
        attack = new AttackData(innateAttack.dieRoll,
                innateAttack.plusToHit + overallWeaponToHit,
                innateAttack.plusToDam + overallWeaponToDam);
        bowAttack = new AttackData(new DieRoll(0,0), overallBowToHit, overallBowToDam);
        for (DungeonItem item : equipment) {
            // if we have a bow/sword, then use that attack instead of our innate attack
            if (item.slot == DungeonItem.Slot.WEAPON) {
                attack = new AttackData(item.attack,
                        item.bonuses[DungeonItem.TO_HIT_IDX] + overallWeaponToHit,
                        item.bonuses[DungeonItem.TO_DAM_IDX] + overallWeaponToDam );
            }
            if (item.slot == DungeonItem.Slot.BOW) {
                bowAttack = new AttackData(item.attack,
                        item.bonuses[DungeonItem.TO_HIT_IDX] + overallBowToHit,
                        item.bonuses[DungeonItem.TO_DAM_IDX] + overallBowToDam );
            }
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

    @Override
    public void gainExperience(GameBackend backend, int exp) {
        super.gainExperience(backend, exp);
        this.experience += exp;
        checkIncreaseCharLevel(backend);
    }

    private void checkIncreaseCharLevel(GameBackend backend) {
        int targetLevel = getTargetLevel();
        while (level <= targetLevel) {
            gainLevel(backend);
        }
    }

    // gets the experience level the character should have
    // based on experience alone.
    private int getTargetLevel() {
        int targetLevel = 0;
        for (int i = 0; i < levelBreakpoints.length; i += 1) {
            if (experience >= levelBreakpoints[i]) {
                targetLevel = i;
            }
        }
        return targetLevel;
    }

    public int getExpToNextLevel() {
        int nextLevel = getTargetLevel() + 1;
        int expBreak = levelBreakpoints[nextLevel];
        return expBreak - experience;
    }

    private void gainLevel(GameBackend backend) {
        backend.logMessage("congrats, you gained a level!");
        this.innateStats.strength += gainStat(this.gainRatios.strRatio, level);
        this.innateStats.dexterity += gainStat(this.gainRatios.dexRatio, level);
        this.innateStats.intelligence += gainStat(this.gainRatios.intRatio, level);
        this.innateStats.constitution += gainStat(this.gainRatios.conRatio, level);
        level += 1;
        updateStats();  // will update MaxHP,
    }

    private int gainStat(double ratio, int level) {
        int currBase = (int) Math.round(ratio * level);
        int nextBase = (int) Math.round(ratio * (level+1));
        return nextBase - currBase;
    }
}
