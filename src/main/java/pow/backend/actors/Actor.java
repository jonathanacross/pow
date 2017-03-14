package pow.backend.actors;

import pow.backend.AttackData;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.AttackUtils;
import pow.backend.conditions.ConditionGroup;
import pow.backend.conditions.ConditionTypes;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;
import pow.backend.event.GameEvent;
import pow.util.DieRoll;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public abstract class Actor extends DungeonObject implements Serializable {

    // Class to simplify constructor arguments
    // when creating an actor (or subclass).
    // TODO: revisit this class; see if can simplify
    public static class Params {
        public final int level;
        public final AttackData attack;
        public final int experience;
        public final boolean friendly; // friendly to the player
        public final boolean invisible;
        public final String requiredItemDrops;
        public final int numDropAttempts;
        public final int maxHealth;
        public final int defense;
        public final int speed;

        public Params(int level,
                      int maxHealth,
                      int defense,
                      int experience,
                      AttackData attack,
                      boolean friendly,
                      boolean invisible,
                      int speed,
                      String requiredItemDrops,
                      int numDropAttempts) {
            this.level = level;
            this.maxHealth = maxHealth;
            this.defense = defense;
            this.experience = experience;
            this.attack = attack;
            this.friendly = friendly;
            this.invisible = invisible;
            this.speed = speed;
            this.requiredItemDrops = requiredItemDrops;
            this.numDropAttempts = numDropAttempts;
        }
    }

    // Holds stats for actors; these control how actors interact with each
    // other and the world. Note that these may be derived from other
    // quantities, e.g., the player's maxHealth may depend on their
    // constitution + equipment, whereas a monster's may just be set at
    // initialization time.
    //
    // Values of these at any particular time may be modified via
    // conditions, which might have temporary changes at any given turn
    public class ActorStats implements Serializable {
        public int maxHealth;
        public int health;
        public int maxMana;
        public int mana;
        public int defense;
        public DieRoll meleeDieRoll;
        public int meleeToHit;
        public int meleeToDam;
        public DieRoll rangedDieRoll;
        public int rangedToHit;
        public int rangedToDam;
        public int speed;
    }

    protected final ActorStats baseStats;
    public final ConditionGroup conditions;
    public final Energy energy;
    public final int experience;
    public final ItemList inventory;
    public final boolean friendly; // friendly to the player
    public final boolean invisible;
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

    public abstract Action act(GameBackend backend);
    public abstract boolean needsInput(GameState gameState);
    public abstract String getPronoun();

    public void setFullHealth() { baseStats.health = getMaxHealth(); }
    public void setFullMana() { baseStats.mana = getMaxMana(); }
    // tries to heal the actor by amount; returns the actual amount healed
    public int increaseHealth(int amount) {
        int increaseAmount = Math.min(amount, getMaxHealth() - getHealth());
        baseStats.health += increaseAmount;
        return increaseAmount;
    }
    // tries to increase the mana of actor by amount; returns the actual amount increased
    public int increaseMana(int amount) {
        int increaseAmount = Math.min(amount, getMaxMana() - getMana());
        baseStats.mana += increaseAmount;
        return increaseAmount;
    }
    public int getMaxHealth() { return baseStats.maxHealth + conditions.get(ConditionTypes.HEALTH).getIntensity(); }
    public int getHealth() { return baseStats.health; }
    public int getMaxMana() { return baseStats.maxMana; }
    public int getMana() { return baseStats.mana; }
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
        this.baseStats.health -= damage;
        if (this.baseStats.health < 0) {
            events.add(AttackUtils.doDie(backend, this));
        }
        return events;
    }

    public boolean canDig() { return false; }  // overridden in Player
    public boolean canSeeInvisible() { return true; }  // overridden in Player
    public boolean canSeeActor(Actor a) { return !a.invisible || this.canSeeInvisible(); }

    public void gainExperience(GameBackend backend, int exp) {} // overridden in Player

    public Actor(DungeonObject.Params objectParams, Params actorParams) {
        super(objectParams);
        this.baseStats = new ActorStats();
        this.energy = new Energy();
        this.level = actorParams.level;
        this.baseStats.maxHealth = actorParams.maxHealth;
        this.baseStats.health = actorParams.maxHealth;
        this.baseStats.maxMana = 0;
        this.baseStats.mana = 0;
        this.baseStats.defense = actorParams.defense;
        this.baseStats.meleeDieRoll = actorParams.attack.dieRoll;
        this.baseStats.meleeToHit = actorParams.attack.plusToHit;
        this.baseStats.meleeToDam = actorParams.attack.plusToDam;
        this.baseStats.rangedDieRoll = actorParams.attack.dieRoll;
        this.baseStats.rangedToHit = actorParams.attack.plusToHit;
        this.baseStats.rangedToDam = actorParams.attack.plusToDam;
        this.baseStats.speed = actorParams.speed;
        this.experience = actorParams.experience;
        this.friendly = actorParams.friendly;
        this.invisible = actorParams.invisible;
        this.requiredItemDrops = actorParams.requiredItemDrops;
        this.numDropAttempts = actorParams.numDropAttempts;
        this.conditions = new ConditionGroup(this);
        this.inventory = new ItemList(20, 99);
        this.gold = 0;
    }
}
