package pow.backend.actors;

import pow.backend.AttackData;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.behavior.ActionBehavior;
import pow.backend.behavior.Behavior;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.LightSource;
import pow.util.Circle;
import pow.util.DieRoll;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends Actor implements Serializable, LightSource {

    public static class GainRatios implements Serializable {
        public final double strRatio;
        public final double dexRatio;
        public final double intRatio;
        public final double conRatio;

        public GainRatios(double strRatio, double dexRatio, double intRatio, double conRatio) {
            this.strRatio = strRatio;
            this.dexRatio = dexRatio;
            this.intRatio = intRatio;
            this.conRatio = conRatio;
        }

        // TODO: put these in data files, eventually
        public static GainRatios getMage() { return new GainRatios(0.9, 0.9, 1.3, 0.9); }
        public static GainRatios getAdventurer() { return new GainRatios(1.0, 1.0, 1.0, 1.0); }
        public static GainRatios getWarrior() { return new GainRatios( 1.1, 1.1, 0.7, 1.1); }
        public static GainRatios getRogue() { return new GainRatios(0.9, 1.1, 1.0, 1.0); }
        public static GainRatios getDragonMaster() { return new GainRatios(1.1, 1.2, 0.9, 1.2); }
    }

    // extra stats specific to players
    public static class PlayerStats implements Serializable {
        public int strength;
        public int dexterity;
        public int intelligence;
        public int constitution;

        public PlayerStats() {
            this.strength = 0;
            this.dexterity = 0;
            this.intelligence = 0;
            this.constitution = 0;
        }
    }

    public final int viewRadius;
    private int lightRadius;
    public final List<DungeonItem> equipment;
    public final Map<DungeonItem.ArtifactSlot, DungeonItem> artifacts;
    private final GainRatios gainRatios;
    public final PlayerStats playerStats;
    public boolean increaseWealth;

    private final AttackData innateAttack; // attack to use if nothing is wielded.
    public int experience;

    public Point floorTarget;
    public Actor monsterTarget;

    public Behavior behavior;

    // computed as totals in MakePlayerExpLevels
    private static final int[] levelBreakpoints = {
            0,
            44,
            175,
            461,
            999,
            1918,
            3392,
            5646,
            8968,
            13720,
            20355,
            29432,
            41632,
            57782,
            78878,
            106110,
            140894,
            184905,
            240112,
            308823,
            393731
    };

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
                GainRatios.getAdventurer(),
                new AttackData(new DieRoll(1, 1), 0, 0)
        );
    }

    private Player(DungeonObject.Params objectParams,
                  GainRatios gainRatios,
                  AttackData innateAttack ) {
        super(objectParams, new Actor.Params( 1, -1, -99, 0, innateAttack, true, false, 0, null, 0));
        this.viewRadius = 11;  // how far can you see, assuming things are lit
        this.lightRadius = -1;  // filled in by updateStats
        this.equipment = new ArrayList<>();
        this.artifacts = new HashMap<>();
        this.gainRatios = gainRatios;
        this.innateAttack = innateAttack;
        this.experience = 0;
        this.playerStats = new PlayerStats();
        updateStats();  // updates currentPlayer stats, as well as much of actor baseStats
        this.baseStats.health = this.baseStats.maxHealth;
        this.baseStats.mana = this.baseStats.maxMana;
        this.floorTarget = null;
        this.monsterTarget = null;
        this.behavior = null;
        this.increaseWealth = false;
    }

    public void addCommand(Action request) {
        this.behavior = new ActionBehavior(this, request);
    }

    public boolean canSeeLocation(GameState gs, Point point) {
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
    public boolean needsInput(GameState gameState) {
        if (this.behavior != null && !this.behavior.canPerform(gameState)) {
            waitForInput();
        }
        return behavior == null;
    }

    public void waitForInput() {
        this.behavior = null;
    }

    @Override
    public Action act(GameBackend backend) {
        return behavior.getAction();
    }

    @Override
    public Point getLocation() { return this.loc; }

    @Override
    public int getLightRadius() {
        return this.lightRadius;
    }

    // update our stats, plus toHit, defense to include current equipped items and other bonuses.
    private void updateStats() {

        // first, get our baseline/innate stats
        int innateStr = (int) Math.round(gainRatios.strRatio * (level + 10));
        int innateDex = (int) Math.round(gainRatios.dexRatio * (level + 10));
        int innateInt = (int) Math.round(gainRatios.intRatio * (level + 10));
        int innateCon = (int) Math.round(gainRatios.conRatio * (level + 10));
        int innateSpd = 0;

        // second, add equipment bonuses for these
        int strBonus = 0;
        int dexBonus = 0;
        int intBonus = 0;
        int conBonus = 0;
        int spdBonus = 0;
        for (DungeonItem item : equipment) {
            strBonus += item.bonuses[DungeonItem.STR_IDX];
            dexBonus += item.bonuses[DungeonItem.DEX_IDX];
            intBonus += item.bonuses[DungeonItem.INT_IDX];
            conBonus += item.bonuses[DungeonItem.CON_IDX];
            spdBonus += item.bonuses[DungeonItem.SPEED_IDX];
        }

        playerStats.strength = innateStr + strBonus;
        playerStats.dexterity = innateDex + dexBonus;
        playerStats.intelligence = innateInt + intBonus;
        playerStats.constitution = innateCon + conBonus;

        // third, compute baseline dependent stats
        DieRoll baseAttackDieRoll = innateAttack.dieRoll;  // will be used if player doesn't wear a weapon
        DieRoll baseBowDieRoll = new DieRoll(0,0);  // will be used if player doesn't wear a bow
        int baseDefense = this.playerStats.dexterity - 7;
        int baseWeaponToHit = 2*(this.playerStats.dexterity - 7);
        int baseWeaponToDam = this.playerStats.strength - 7;
        int baseBowToHit = (int) Math.round(1.50 * (this.playerStats.dexterity - 7));
        int baseBowToDam = (int) Math.round(0.75 * (this.playerStats.strength - 7));

        // fourth, add equipment bonuses
        int defBonus = 0;
        int weapToHitBonus = 0;
        int weapToDamBonus = 0;
        int bowToHitBonus = 0;
        int bowToDamBonus = 0;

        for (DungeonItem item : equipment) {
            defBonus += item.defense + item.bonuses[DungeonItem.DEF_IDX];
            // Only add non bow/weapon (i.e. from rings/amulets) for toHit/toDam bonuses.
            // The weapon and bow bonuses will be applied to the weapon and bow, separately, later.
            if (item.slot != DungeonItem.Slot.BOW && item.slot != DungeonItem.Slot.WEAPON) {
                // toHit, toDam applies to both bow and weapon for
                weapToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                weapToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                bowToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                bowToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
            }
            // if we have a bow/sword, then use that attack instead of our innate attack
            if (item.slot == DungeonItem.Slot.WEAPON) {
                weapToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                weapToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                baseAttackDieRoll = item.attack;
            }
            if (item.slot == DungeonItem.Slot.BOW) {
                bowToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                bowToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                baseBowDieRoll = item.attack;
            }
        }

        int c = this.playerStats.constitution;
        int i = this.playerStats.intelligence;
        this.baseStats.maxHealth = (int) Math.round(0.5 * c * c - 7 * c + 30);
        this.baseStats.maxMana = (int) Math.round(0.5 * i * i - 7 * i + 30);
        this.baseStats.defense = baseDefense + defBonus;
        this.baseStats.meleeDieRoll = baseAttackDieRoll;
        this.baseStats.meleeToHit = baseWeaponToHit + weapToHitBonus;
        this.baseStats.meleeToDam = baseWeaponToDam + weapToDamBonus;
        this.baseStats.rangedDieRoll = baseBowDieRoll;
        this.baseStats.rangedToHit = baseBowToHit + bowToHitBonus;
        this.baseStats.rangedToDam = baseBowToDam + bowToDamBonus;
        this.baseStats.speed = innateSpd + spdBonus;

        updateWealthStatus();
        updateLightRadius();
    }

    private void updateWealthStatus() {
        this.increaseWealth = false;
        for (DungeonItem item : equipment) {
            if (item.bonuses[DungeonItem.WEALTH_IDX] > 0) {
                this.increaseWealth = true;
            }
        }
    }

    private void updateLightRadius() {
        this.lightRadius = 4; // slightly better than a candle
        if (artifacts.containsKey(DungeonItem.ArtifactSlot.LANTERN)) {
            this.lightRadius = 8;
        }
        if (artifacts.containsKey(DungeonItem.ArtifactSlot.LANTERN2)) {
            this.lightRadius = 11;
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

    public void addArtifact(DungeonItem item) {
        artifacts.put(item.artifactSlot, item);
        updateStats();
    }

    @Override
    public void gainExperience(GameBackend backend, int exp) {
        super.gainExperience(backend, exp);
        this.experience += exp;
        checkIncreaseCharLevel(backend);
    }

    private void checkIncreaseCharLevel(GameBackend backend) {
        if (level >= levelBreakpoints.length) return;

        int targetLevel = getTargetLevel();
        while (level <= targetLevel) {
            gainLevel(backend);
        }
    }

    // gets the experience level the character should have
    // based on experience alone.
    private int getTargetLevel() {
        int targetLevel = 0;
        for (int i = 0; i < levelBreakpoints.length; i++) {
            if (experience >= levelBreakpoints[i]) {
                targetLevel = i;
            }
        }
        return targetLevel;
    }

    public int getExpToNextLevel() {
        if (level >= levelBreakpoints.length) return 0;

        int nextLevel = getTargetLevel() + 1;
        int expBreak = levelBreakpoints[nextLevel];
        return expBreak - experience;
    }

    // stat gain formulas: here are initial scale constants
    //        mage    adventurer    warrior    dragm    rogue
    // str    0.9    1    1.1    1.1    0.9
    // dex    0.9    1    1.1    1.2    1.1
    // int    1.3    1    0.7    0.9    1
    // con    0.9    1    1.1    1.2    1
    // total    4    4    4    4.4    4
    //
    // Then, stat = scale*(level + 10)
    // from here, can get  HP/MP by one of:
    // innate HP = 0.55 str^2 - 6.94*str + 23  (nice quadratic, like it better than exponential)
    // innate MP = 0.51 int^2 - 8.00*int + 37
    // hp or mp = 0.5 * stat^2 - 7*stat + 30  <- pretty nice, and works all right for both


    private void gainLevel(GameBackend backend) {
        backend.logMessage("congrats, you gained a level!");
        level++;
        updateStats();  // will update MaxHP,
    }

    public boolean hasBowEquipped() {
        for (DungeonItem item : equipment)  {
            if (item.slot.equals(DungeonItem.Slot.BOW)) return true;
        }
        return false;
    }

    @Override
    public boolean canDig() {
        return artifacts.containsKey(DungeonItem.ArtifactSlot.PICKAXE);
    }

    @Override
    public boolean canSeeInvisible() {
        return artifacts.containsKey(DungeonItem.ArtifactSlot.GLASSES);
    }

    public DungeonItem findArrows() {
        for (DungeonItem item : inventory.items)  {
            if (item.flags.arrow) return item;
        }
        return null;
    }
}
