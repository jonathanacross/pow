package pow.backend.actors;

import pow.backend.AttackData;
import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.conditions.Condition;
import pow.backend.conditions.Health;
import pow.backend.conditions.Poison;
import pow.backend.conditions.Speed;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;

import java.io.Serializable;

public abstract class Actor extends DungeonObject implements Serializable {

    public class Conditions {
        public Health health;
        public Poison poison;
        public Speed speed;

        public Conditions(Actor actor) {
            health = new Health(actor);
            poison = new Poison(actor);
            speed = new Speed(actor);
        }

        public void update(GameBackend backend) {
            health.update(backend);
            poison.update(backend);
            speed.update(backend);
        }
    }

    public Energy energy;

    public int maxHealth;
    public int health;
    public int maxMana;
    public int mana;
    public int experience;
    public int defense; // chance of hitting is related to toHit and defense
    public Conditions conditions;
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

    public void takeDamage(GameBackend backend, int damage) {
        this.health -= damage;
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
        this.conditions = new Conditions(this);
        this.inventory = new ItemList(20, 99);
        this.maxMana = 0;
        this.mana = 0;
        this.gold = 0;
    }
}
