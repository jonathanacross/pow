package pow.backend.actors;

import pow.backend.AttackData;
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
    public int defense; // chance of hitting is related to toHit and defense
    public AttackData attack;
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
        public int defense;
        public AttackData attack;
        public boolean friendly; // friendly to the player
        public int speed;

        public Params(int maxHealth, int defense, AttackData attack, boolean friendly, int speed) {
            this.maxHealth = maxHealth;
            this.defense = defense;
            this.attack = attack;
            this.friendly = friendly;
            this.speed = speed;
        }
    }

    public Actor(DungeonObject.Params objectParams, Params actorParams) {
        super(objectParams);
        this.energy = new Energy();
        this.health = actorParams.maxHealth;
        this.maxHealth = actorParams.maxHealth;
        this.defense = actorParams.defense;
        this.attack = actorParams.attack;
        this.friendly = actorParams.friendly;
        this.speed = actorParams.speed;
        this.inventory = new ItemList(20, 99);
    }
}
