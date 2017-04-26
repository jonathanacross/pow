package pow.backend.actors;

import pow.backend.*;
import pow.backend.action.Action;
import pow.backend.behavior.ActionBehavior;
import pow.backend.behavior.Behavior;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.LightSource;
import pow.backend.dungeon.gen.SpellData;
import pow.backend.event.GameEvent;
import pow.util.Circle;
import pow.util.DieRoll;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.*;

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

    public final int viewRadius;
    private int lightRadius;
    public final List<DungeonItem> equipment;
    public final Map<DungeonItem.ArtifactSlot, DungeonItem> artifacts;
    private final GainRatios gainRatios;
    public boolean increaseWealth;
    private boolean winner;

    public int experience;

    public Point floorTarget;
    public Actor monsterTarget;

    public Behavior behavior;

    // computed as totals in MakePlayerExpLevels
    private static final int[] levelBreakpoints = {
            0,
            46,
            295,
            758,
            1477,
            2518,
            3970,
            5950,
            8609,
            12143,
            16803,
            22912,
            30883,
            41243,
            54668,
            72019,
            94395,
            123198,
            160214,
            207719,
            268610
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
                GainRatios.getAdventurer()
        );
    }

    private Player(DungeonObject.Params objectParams,
                   GainRatios gainRatios) {
        super(objectParams, new Actor.Params(
                1,
                0,
                true,
                false,
                false,
                null,
                0,
                10,
                10,
                10,
                10,
                0,
                Arrays.asList(
                        SpellData.getSpell("magic arrow"),
                        SpellData.getSpell("phase"),
                        SpellData.getSpell("circle cut"),
                        SpellData.getSpell("lesser heal"),
                        SpellData.getSpell("ice bolt"),
                        SpellData.getSpell("fireball"),
                        SpellData.getSpell("rift"),
                        SpellData.getSpell("quake")
                )));
        this.viewRadius = 11;  // how far can you see, assuming things are lit
        this.lightRadius = -1;  // filled in by updateStats
        this.equipment = new ArrayList<>();
        this.artifacts = new HashMap<>();
        this.gainRatios = gainRatios;
        this.experience = 0;
        updateStats();
        this.health = this.baseStats.maxHealth;
        this.mana = this.baseStats.maxMana;
        this.floorTarget = null;
        this.monsterTarget = null;
        this.behavior = null;
        this.increaseWealth = false;
        this.winner = false;
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

        int innateStr = (int) Math.round(gainRatios.strRatio * (level + 6));
        int innateDex = (int) Math.round(gainRatios.dexRatio * (level + 6));
        int innateInt = (int) Math.round(gainRatios.intRatio * (level + 6));
        int innateCon = (int) Math.round(gainRatios.conRatio * (level + 6));
        int innateSpd = 0;

        this.baseStats = new ActorStats(innateStr, innateDex, innateInt, innateCon, innateSpd, equipment);
        this.health = Math.min(health, getMaxHealth());
        this.mana = Math.min(mana, getMaxMana());

        updateWealthStatus();
        updateLightRadius();
        updateBagSize();
        updateAquatic();
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
        this.lightRadius = GameConstants.PLAYER_SMALL_LIGHT_RADIUS;
        if (artifacts.containsKey(DungeonItem.ArtifactSlot.LANTERN)) {
            this.lightRadius = GameConstants.PLAYER_MED_LIGHT_RADIUS;
        }
        if (artifacts.containsKey(DungeonItem.ArtifactSlot.LANTERN2)) {
            this.lightRadius = GameConstants.PLAYER_LARGE_LIGHT_RADIUS;
        }
    }

    private void updateBagSize() {
        if (this.artifacts.containsKey(DungeonItem.ArtifactSlot.BAG)) {
            this.inventory.increaseMaxPerSlot(GameConstants.PLAYER_EXPANDED_ITEMS_PER_SLOT);
        }
    }

    private void updateAquatic() {
        this.aquatic = this.artifacts.containsKey(DungeonItem.ArtifactSlot.FLOAT);
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

    public List<GameEvent> addArtifact(DungeonItem item) {
        artifacts.put(item.artifactSlot, item);
        updateStats();
        List<GameEvent> events = new ArrayList<>();

        // check for a win!
        if (!winner && hasAllPearls()) {
            winner = true;
            events.add(GameEvent.WonGame());
        }

        return events;
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

    public boolean hasGasMask() {
        return artifacts.containsKey(DungeonItem.ArtifactSlot.GASMASK);
    }
    public boolean hasHeatSuit() { return artifacts.containsKey(DungeonItem.ArtifactSlot.HEATSUIT); }
    public boolean hasKey() { return artifacts.containsKey(DungeonItem.ArtifactSlot.KEY); }
    public boolean hasAllPearls() {
        return  artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL1) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL2) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL3) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL4) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL5) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL6) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL7) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL8);
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

    public boolean isWinner() { return winner; }

    // TODO: put in actor class?
    public Point getTarget() {
        if (floorTarget != null) {
            return floorTarget;
        } else if (monsterTarget != null) {
            return monsterTarget.loc;
        } else {
            return null;
        }
    }
}
