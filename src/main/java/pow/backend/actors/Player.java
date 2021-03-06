package pow.backend.actors;

import pow.backend.*;
import pow.backend.action.Action;
import pow.backend.ai.StepMovement;
import pow.backend.behavior.AiBehavior;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;
import pow.backend.dungeon.LightSource;
import pow.util.Circle;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Player extends Actor implements Serializable, LightSource {

    public final int viewRadius;
    private int lightRadius;
    public final ItemList equipment;
    private final GainRatios gainRatios;
    public boolean increaseWealth;
    public boolean winner;

    public int experience;

    public final Target target;
    public Party party;  // link back to common data in the party.

    // computed as totals in MakePlayerExpLevels
    private static final int[] levelBreakpoints = {
            0,
            58,
            279,
            621,
            1093,
            1720,
            2540,
            3601,
            4966,
            6715,
            8951,
            11805,
            15442,
            20072,
            25962,
            33450,
            42964,
            55048,
            70390,
            89863,
            114573
    };

    public Player(DungeonObject.Params objectParams,
                   GainRatios gainRatios,
                   List<SpellParams> spells, Abilities abilities, List<DungeonItem> startItems) {
        super(objectParams, new Actor.Params(
                1,
                0,
                true,
                false,
                false,
                new StepMovement(),
                Collections.emptyList(),
                0,
                getInnateStr(gainRatios, 1),
                getInnateDex(gainRatios, 1),
                getInnateInt(gainRatios, 1),
                getInnateCon(gainRatios, 1),
                getInnateSpeed(gainRatios, 1),
                spells,
                abilities));
        this.viewRadius = 11;  // how far can you see, assuming things are lit
        this.lightRadius = GameConstants.PLAYER_SMALL_LIGHT_RADIUS;
        this.equipment = new ItemList();
        this.gainRatios = gainRatios;
        this.experience = 0;
        //updateStats();
        this.health = this.baseStats.maxHealth;
        this.mana = this.baseStats.maxMana;
        this.target = new Target();
        this.increaseWealth = false;
        this.winner = false;
        this.party = null;

        for (DungeonItem item : startItems) {
            if (item.slot != DungeonItem.Slot.NONE) {
                this.equipment.add(item);
            } else {
                this.inventory.add(item);
            }
        }
    }

    @Override
    public boolean canSeeLocation(GameState gs, Point point) {
        // must be on the map, within the player's view radius, and must be lit
        return (gs.getCurrentMap().isOnMap(point.x, point.y) &&
                (MathUtils.dist2(loc, point) <= Circle.getRadiusSquared(viewRadius)) &&
                (gs.getCurrentMap().map[point.x][point.y].brightness > 0));
    }

    @Override
    public String getNoun() { return name; }

    public void setAutoplay(GameState gameState, boolean autoPlay) {
        if (autoPlay) {
            this.behavior = new AiBehavior(this, gameState);
        } else {
            this.behavior = null;
        }
    }

    @Override
    public boolean needsInput(GameState gameState) {
        if (this.behavior != null && !this.behavior.canPerform(gameState)) {
            clearBehavior();
        }
        return behavior == null;
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

    private static int getInnateStr(GainRatios gainRatios, int level) {
        return (int) Math.round(gainRatios.strRatio * (level + 6));
    }

    private static int getInnateDex(GainRatios gainRatios, int level) {
        return (int) Math.round(gainRatios.dexRatio * (level + 6));
    }

    private static int getInnateInt(GainRatios gainRatios, int level) {
        return (int) Math.round(gainRatios.intRatio * (level + 6));
    }

    private static int getInnateCon(GainRatios gainRatios, int level) {
        return (int) Math.round(gainRatios.conRatio * (level + 6));
    }

    private static int getInnateSpeed(GainRatios gainRatios, int level) {
        return (int) Math.round(Math.max((level - 10) * 0.5 * gainRatios.speedRatio, 0));
    }

    // Update our stats, plus toHit, defense to include current equipped items and other bonuses.
    public void updateStats() {
        int innateStr = getInnateStr(gainRatios, level);
        int innateDex = getInnateDex(gainRatios, level);
        int innateInt = getInnateInt(gainRatios, level);
        int innateCon = getInnateCon(gainRatios, level);
        int innateSpd = getInnateSpeed(gainRatios, level);

        // Turtle shell only confers bonus if the wearer isn't wearing anything
        // (notably, for the pet).
        int extraResistBonus = (equipment.items.isEmpty() && party.artifacts.hasTurtleShell()) ? 2 : 0;

        this.baseStats = new ActorStats(innateStr, innateDex, innateInt, innateCon, innateSpd, equipment.items,
                abilities.archeryBonus, extraResistBonus);
        this.health = Math.min(health, getMaxHealth());
        this.mana = Math.min(mana, getMaxMana());

        updateWealthStatus();
        updateLightRadius();
        updateBagSize();
        updateAquatic();
    }

    private void updateWealthStatus() {
        this.increaseWealth = false;
        for (DungeonItem item : equipment.items) {
            if (item.bonuses[DungeonItem.WEALTH_IDX] > 0) {
                this.increaseWealth = true;
            }
        }
    }

    private void updateLightRadius() {
        this.lightRadius = party.artifacts.getLightRadius();
    }

    private void updateBagSize() {
        if (party.artifacts.hasBag()) {
            this.inventory.increaseMaxPerSlot(GameConstants.PLAYER_EXPANDED_ITEMS_PER_SLOT);
        }
    }

    private void updateAquatic() {
        this.aquatic = party.artifacts.hasFloat();
    }

    // returns the old item, if any
    public DungeonItem wear(DungeonItem item) {
        DungeonItem.Slot slot = item.slot;
        for (int i = 0; i < equipment.size(); i++) {
            if (equipment.get(i).slot == slot) {
                // replace old item
                DungeonItem oldItem = equipment.items.remove(i);
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
        DungeonItem item = equipment.items.remove(idx);
        updateStats();
        return item;
    }

    @Override
    public void gainExperience(GameBackend backend, int experience, Actor source) {
        super.gainExperience(backend, experience, source);
        this.experience += experience;
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

    private void gainLevel(GameBackend backend) {
        backend.logMessage(getNoun() + " gained a level!", MessageLog.MessageType.GAME_EVENT);
        level++;
        updateStats();
    }

    public boolean hasBowEquipped() {
        for (DungeonItem item : equipment.items)  {
            if (item.slot.equals(DungeonItem.Slot.BOW)) return true;
        }
        return false;
    }

    @Override
    public boolean canDig() {
        return party.artifacts.hasPickAxe();
    }

    @Override
    public boolean canSeeInvisible() {
        return party.artifacts.hasGlasses();
    }

    @Override
    public boolean canSeeTraps() { return party.artifacts.hasXRayScope(); }

    public DungeonItem findArrows() {
        for (DungeonItem item : inventory.items)  {
            if (item.flags.arrow) return item;
        }
        return null;
    }

    public boolean isWinner() { return winner; }

    @Override
    public Point getTarget() { return target.get(); }
}
