package pow.backend.actors;

import pow.backend.AttackData;
import pow.backend.GameBackend;
import pow.backend.action.Action;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;

import java.io.Serializable;

public abstract class Actor extends DungeonObject implements Serializable {
    public Energy energy;

    public int maxHealth;
    public int health;
    public int maxMana;
    public int mana;
    public int experience;
    public int defense; // chance of hitting is related to toHit and defense
    public AttackData attack;
    public ItemList inventory;

    public boolean friendly; // friendly to the player
    public int speed;
    public int level;

    public abstract Action act(GameBackend backend);

    public abstract boolean needsInput();

    public abstract String getPronoun();

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

        public Params(int level, int maxHealth, int defense, int experience, AttackData attack, boolean friendly, int speed) {
            this.level = level;
            this.maxHealth = maxHealth;
            this.defense = defense;
            this.experience = experience;
            this.attack = attack;
            this.friendly = friendly;
            this.speed = speed;
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
        this.speed = actorParams.speed;
        this.inventory = new ItemList(20, 99);
        this.maxMana = 0;
        this.mana = 0;
    }
}
