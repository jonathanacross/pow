package pow.backend.actors;

import pow.backend.AttackData;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.AttackUtils;
import pow.backend.conditions.Conditions;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Actor extends DungeonObject implements Serializable {

    public class ConditionSet implements Serializable {
        public Conditions.Health health;
        public Conditions.Poison poison;
        public Conditions.Speed speed;
        public Conditions.ToHit toHit;
        public Conditions.ToDam toDam;
        public Conditions.Defense defense;

        public ConditionSet(Actor actor) {
            health = new Conditions.Health(actor);
            poison = new Conditions.Poison(actor);
            speed = new Conditions.Speed(actor);
            toHit = new Conditions.ToHit(actor);
            toDam = new Conditions.ToDam(actor);
            defense = new Conditions.Defense(actor);
        }

        public List<GameEvent> update(GameBackend backend) {
            List<GameEvent> events = new ArrayList<>();
            events.addAll(health.update(backend));
            events.addAll(poison.update(backend));
            events.addAll(speed.update(backend));
            events.addAll(toHit.update(backend));
            events.addAll(toDam.update(backend));
            events.addAll(defense.update(backend));
            return events;
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
    //
    // TODO: Should this be an interface? (might be annoying to set health..)
//    public class ActorStats implements Serializable {
//        public int maxHealth;
//        public int health;
//        public int maxMana;
//        public int mana;
//        public int defense;
//        public int toHit;
//        public int toDam;
//        public int speed;
//        // resistances here as well
//    }

    public Energy energy;

    protected int maxHealth;
    public int health;
    public int maxMana;
    public int mana;
    public int experience;
    protected int defense; // chance of hitting is related to attack/toHit and defense
    public ConditionSet conditions; // TODO: better name for ConditionSet
    public AttackData attack;
    public ItemList inventory;

    public boolean friendly; // friendly to the player
    //private int speed;

    public int level;  // player only?
    public int gold;
    // TODO: perhaps monsters should be generated w/ items, then they only
    // drop their items when they die, and these 2 variables wouldn't be needed.
    // (Or could be renamed to expectedNumItems, and moved to monster.)
    public int numDropAttempts; // number of attempts of dropping an item, monster only?
    public String requiredItemDrops;

    public abstract Action act(GameBackend backend);

    public abstract boolean needsInput(GameState gameState);

    public abstract String getPronoun();
    protected abstract int getBaseSpeed();
    public int getSpeed() { return getBaseSpeed() + conditions.speed.getIntensity(); }
    public int getMaxHealth() { return maxHealth + conditions.health.getIntensity(); }
    public int getDefense() { return defense + conditions.defense.getIntensity(); }

    public List<GameEvent> takeDamage(GameBackend backend, int damage) {
        this.health -= damage;
        if (this.health < 0) {
            return AttackUtils.doDie(backend, this);
        }
        return new ArrayList<>();
    }

    public void gainExperience(GameBackend backend, int exp) {} // overridden in player

    public static class Params {
        public int level;
        public int maxHealth;
        public int defense;
        public int experience;
        public AttackData attack;
        public boolean friendly; // friendly to the player
        public int speed;
        public String requiredItemDrops;
        public int numDropAttempts;

        public Params(int level,
                      int maxHealth,
                      int defense,
                      int experience,
                      AttackData attack,
                      boolean friendly,
                      int speed,
                      String requiredItemDrops,
                      int numDropAttempts) {
            this.level = level;
            this.maxHealth = maxHealth;
            this.defense = defense;
            this.experience = experience;
            this.attack = attack;
            this.friendly = friendly;
            this.speed = speed;
            this.requiredItemDrops = requiredItemDrops;
            this.numDropAttempts = numDropAttempts;
        }
    }

    public Actor(DungeonObject.Params objectParams, Params actorParams) {
        super(objectParams);
        this.energy = new Energy();
        this.level = actorParams.level;
        this.health = actorParams.maxHealth;
        this.maxHealth = actorParams.maxHealth;
        this.experience = actorParams.experience;
        this.defense = actorParams.defense;
        this.attack = actorParams.attack;
        this.friendly = actorParams.friendly;
        //this.speed = actorParams.speed;
        this.requiredItemDrops = actorParams.requiredItemDrops;
        this.numDropAttempts = actorParams.numDropAttempts;
        this.conditions = new ConditionSet(this);
        this.inventory = new ItemList(20, 99);
        this.maxMana = 0;
        this.mana = 0;
        this.gold = 0;
    }
}
