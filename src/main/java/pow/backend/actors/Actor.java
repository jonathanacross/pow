package pow.backend.actors;

import pow.backend.*;
import pow.backend.action.Action;
import pow.backend.action.AttackUtils;
import pow.backend.conditions.ConditionGroup;
import pow.backend.conditions.ConditionTypes;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Actor extends DungeonObject implements Serializable {

    // Class to simplify constructor arguments
    // when creating an actor (or subclass).
    // TODO: revisit this class; see if can simplify
    public static class Params {
        public final int level;
        public final int experience;
        public final boolean friendly; // friendly to the player
        public final boolean invisible;
        public final boolean aquatic;
        public final String requiredItemDrops;
        public final int numDropAttempts;
        public final int strength;
        public final int dexterity;
        public final int intelligence;
        public final int constitution;
        public final int speed;
        public final List<SpellParams> spells;

        public Params(int level,
                      int experience,
                      boolean friendly,
                      boolean invisible,
                      boolean aquatic,
                      String requiredItemDrops,
                      int numDropAttempts,
                      int strength,
                      int dexterity,
                      int intelligence,
                      int constitution,
                      int speed,
                      List<SpellParams> spells) {
            this.level = level;
            this.experience = experience;
            this.friendly = friendly;
            this.invisible = invisible;
            this.aquatic = aquatic;
            this.requiredItemDrops = requiredItemDrops;
            this.numDropAttempts = numDropAttempts;
            this.strength = strength;
            this.dexterity = dexterity;
            this.intelligence = intelligence;
            this.constitution = constitution;
            this.speed = speed;
            this.spells = spells;
        }
    }

    public ActorStats baseStats;
    public int health;
    public int mana;

    public final ConditionGroup conditions;
    public final Energy energy;
    public final int experience;
    public final ItemList inventory;
    public final boolean friendly; // friendly to the player
    public final boolean invisible;
    public boolean aquatic;  // can go on water
    public final boolean terrestrial; // can go on land
    public int level;
    public int gold;
    // Ideally, we would make all items for monsters at
    // monster creation time, since these fields are really
    // only applicable to monsters. However, since the player
    // can wield items that can increase drop frequency
    // we don't know how many items there should be until
    // they die.
    public final int numDropAttempts; // number of attempts of dropping an item, monster only?
    public final String requiredItemDrops;
    public final List<SpellParams> spells;

    public abstract Action act(GameBackend backend);
    public abstract boolean needsInput(GameState gameState);
    public abstract String getPronoun();

    public void setFullHealth() { health = getMaxHealth(); }
    public void setFullMana() { mana = getMaxMana(); }
    // tries to heal the actor by amount; returns the actual amount healed
    public int increaseHealth(int amount) {
        int increaseAmount = Math.min(amount, getMaxHealth() - getHealth());
        health += increaseAmount;
        return increaseAmount;
    }
    // tries to increase the mana of actor by amount; returns the actual amount increased
    public int increaseMana(int amount) {
        int increaseAmount = Math.min(amount, getMaxMana() - getMana());
        mana += increaseAmount;
        return increaseAmount;
    }
    public void useMana(int amount) {
        mana -= Math.min(amount, getMana());
    }
    public int getMaxHealth() { return baseStats.maxHealth + conditions.get(ConditionTypes.HEALTH).getIntensity(); }
    public int getHealth() { return health; }
    public int getMaxMana() { return baseStats.maxMana; }
    public int getMana() { return mana; }
    public int getDefense() { return baseStats.defense + conditions.get(ConditionTypes.DEFENSE).getIntensity(); }
    public int getSpeed() { return baseStats.speed + conditions.get(ConditionTypes.SPEED).getIntensity(); }

    public AttackData getPrimaryAttack() {
        return new AttackData(
                baseStats.meleeDieRoll,
                baseStats.meleeToHit + conditions.get(ConditionTypes.TO_HIT).getIntensity(),
                baseStats.meleeToDam + conditions.get(ConditionTypes.TO_DAM).getIntensity());
    }
    public AttackData getSecondaryAttack() {
        return new AttackData(
                baseStats.rangedDieRoll,
                baseStats.rangedToHit + conditions.get(ConditionTypes.TO_HIT).getIntensity(),
                baseStats.rangedToDam + conditions.get(ConditionTypes.TO_DAM).getIntensity());
    }

    public List<GameEvent> takeDamage(GameBackend backend, int damage) {
        List<GameEvent> events = new ArrayList<>();
        this.health -= damage;
        if (this.health < 0) {
            events.add(AttackUtils.doDie(backend, this));
        }
        return events;
    }

    public boolean canDig() { return false; }  // overridden in Player
    public boolean canSeeInvisible() { return true; }  // overridden in Player
    public boolean canSeeActor(Actor a) { return !a.invisible || this.canSeeInvisible(); }

    public void gainExperience(GameBackend backend, int experience, Actor source) {} // overridden in Player

    public Actor(DungeonObject.Params objectParams, Params actorParams) {
        super(objectParams);
        this.energy = new Energy();
        this.level = actorParams.level;
        this.baseStats = new ActorStats(
                actorParams.strength,
                actorParams.dexterity,
                actorParams.intelligence,
                actorParams.constitution,
                actorParams.speed,
                Collections.emptyList());
        this.health = baseStats.maxHealth;
        this.mana = baseStats.maxMana;
        this.spells = actorParams.spells;
        this.experience = actorParams.experience;
        this.friendly = actorParams.friendly;
        this.invisible = actorParams.invisible;
        this.aquatic = actorParams.aquatic;
        this.terrestrial = !actorParams.aquatic;
        this.requiredItemDrops = actorParams.requiredItemDrops;
        this.numDropAttempts = actorParams.numDropAttempts;
        this.conditions = new ConditionGroup(this);
        this.inventory = new ItemList(GameConstants.ACTOR_ITEM_LIST_SIZE, GameConstants.ACTOR_DEFAULT_ITEMS_PER_SLOT);
        this.gold = 0;
    }
}
