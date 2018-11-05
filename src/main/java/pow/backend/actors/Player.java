package pow.backend.actors;

import pow.backend.*;
import pow.backend.action.Action;
import pow.backend.actors.ai.StepMovement;
import pow.backend.behavior.AiBehavior;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;
import pow.backend.dungeon.LightSource;
import pow.backend.event.GameEvent;
import pow.util.Circle;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.*;

public class Player extends Actor implements Serializable, LightSource {

    public final int viewRadius;
    private int lightRadius;
    public final ItemList equipment;
    // TODO: move artifacts out, e.g., to a party
    public final Artifacts artifacts;
    private final GainRatios gainRatios;
    public boolean increaseWealth;
    private boolean winner;
    public boolean autoPlay;  // whether this character is controlled by computer

    public int experience;

    public Point floorTarget;
    public Actor monsterTarget;

    // TODO: move this out, e.g., to party
    public final Knowledge knowledge;

    // computed as totals in MakePlayerExpLevels
    private static final int[] levelBreakpoints = {
            0,
            40,
            256,
            661,
            1293,
            2213,
            3503,
            5272,
            7661,
            10853,
            15086,
            20664,
            27982,
            37546,
            50006,
            66197,
            87191,
            114362,
            149470,
            194771,
            253152
    };

    // default (empty) player
    public Player() {
        this(new DungeonObject.Params(
                        "player", // id
                        "", // name
                        "human_adventurer", // image
                        "yourself", // description
                        new Point(-1, -1), // location -- will be updated later
                        true), // solid
             GainRatiosData.getGainRatios("player adventurer"),
             Collections.emptyList(),
                false);
    }

    public Player(DungeonObject.Params objectParams,
                   GainRatios gainRatios,
                   List<SpellParams> spells,
                  boolean autoPlay) {
        super(objectParams, new Actor.Params(
                1,
                0,
                true,
                false,
                false,
                new StepMovement(),
                null,
                0,
                10,
                10,
                10,
                10,
                0,
                spells));
        this.viewRadius = 11;  // how far can you see, assuming things are lit
        this.lightRadius = -1;  // filled in by updateStats
        this.equipment = new ItemList();
        this.artifacts = new Artifacts();
        this.gainRatios = gainRatios;
        this.experience = 0;
        updateStats();
        this.health = this.baseStats.maxHealth;
        this.mana = this.baseStats.maxMana;
        this.floorTarget = null;
        this.monsterTarget = null;
        this.increaseWealth = false;
        this.winner = false;
        this.knowledge = new Knowledge();
        this.autoPlay = autoPlay;
   }

    @Override
    public boolean canSeeLocation(GameState gs, Point point) {
        // must be on the map, within the player's view radius, and must be lit
        return (gs.getCurrentMap().isOnMap(point.x, point.y) &&
                (MathUtils.dist2(loc, point) <= Circle.getRadiusSquared(viewRadius)) &&
                (gs.getCurrentMap().map[point.x][point.y].brightness > 0));
    }

    @Override
    public String getPronoun() { return name; }

    public void setAutoplay(GameState gameState, boolean autoPlay) {
        this.autoPlay = autoPlay;
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

    // update our stats, plus toHit, defense to include current equipped items and other bonuses.
    private void updateStats() {

        int innateStr = (int) Math.round(gainRatios.strRatio * (level + 6));
        int innateDex = (int) Math.round(gainRatios.dexRatio * (level + 6));
        int innateInt = (int) Math.round(gainRatios.intRatio * (level + 6));
        int innateCon = (int) Math.round(gainRatios.conRatio * (level + 6));
        int innateSpd = 0;

        this.baseStats = new ActorStats(innateStr, innateDex, innateInt, innateCon, innateSpd, equipment.items);
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
        this.lightRadius = artifacts.getLightRadius();
    }

    private void updateBagSize() {
        if (this.artifacts.hasBag()) {
            this.inventory.increaseMaxPerSlot(GameConstants.PLAYER_EXPANDED_ITEMS_PER_SLOT);
        }
    }

    private void updateAquatic() {
        this.aquatic = this.artifacts.hasFloat();
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

    public List<GameEvent> addArtifact(DungeonItem item) {
        artifacts.add(item);
        updateStats();
        List<GameEvent> events = new ArrayList<>();

        // check for getting a pet
        if (item.artifactSlot.equals(DungeonItem.ArtifactSlot.PETSTATUE)) {
            events.add(GameEvent.GotPet());
        }

        // check for a win!
        if (!winner && artifacts.hasAllPearls()) {
            winner = true;
            events.add(GameEvent.WonGame());
        }

        return events;
    }

    @Override
    public void gainExperience(GameBackend backend, int experience, Actor source) {
        super.gainExperience(backend, experience, source);
        this.experience += experience;
        // TODO: big hack here -- don't include your pet in the kill count..
        // shouldn't have to key off the name 'pet', though
        if (source != null && !source.id.equals("pet")) {
            this.knowledge.incrementKillCount(source);
        }
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
        backend.logMessage(this.name + " gained a level!", MessageLog.MessageType.GAME_EVENT);
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
        return artifacts.hasPickAxe();
    }

    @Override
    public boolean canSeeInvisible() {
        return artifacts.hasGlasses();
    }

    public DungeonItem findArrows() {
        for (DungeonItem item : inventory.items)  {
            if (item.flags.arrow) return item;
        }
        return null;
    }

    public boolean isWinner() { return winner; }

    @Override
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
