package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.action.Action;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.ItemList;
import pow.util.DieRoll;

import java.io.Serializable;

public abstract class Actor extends DungeonObject implements Serializable {
    public Energy energy;

    public int maxHealth;
    public int health;
    public int dexterity;  // chance of hitting is related to dexterity and defense
    public int defense;
    public DieRoll attackDamage;
    public ItemList inventory;

    public boolean friendly; // friendly to the player
    public int speed;

    public abstract Action act(GameBackend backend);

    public abstract boolean needsInput();

    public abstract String getPronoun();

    public void takeDamage(GameBackend backend, int damage) {
        this.health -= damage;
    }

    public static class Params {
        public int maxHealth;
        public int dexterity;
        public int defense;
        public DieRoll attackDamage;
        public boolean friendly; // friendly to the player
        public int speed;

        public Params(int maxHealth, int dexterity, int defense, DieRoll attackDamage, boolean friendly, int speed) {
            this.maxHealth = maxHealth;
            this.dexterity = dexterity;
            this.defense = defense;
            this.attackDamage = attackDamage;
            this.friendly = friendly;
            this.speed = speed;
        }
    }

    public Actor(DungeonObject.Params objectParams, Params actorParams) {
        super(objectParams);
        this.energy = new Energy();
        this.health = actorParams.maxHealth;
        this.maxHealth = actorParams.maxHealth;
        this.dexterity = actorParams.dexterity;
        this.defense = actorParams.defense;
        this.attackDamage = actorParams.attackDamage;
        this.friendly = actorParams.friendly;
        this.speed = actorParams.speed;
        this.inventory = new ItemList(20, 99);
    }
}
