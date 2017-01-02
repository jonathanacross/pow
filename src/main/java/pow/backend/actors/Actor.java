package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.action.Action;
import pow.backend.dungeon.DungeonObject;

import java.io.Serializable;

public abstract class Actor extends DungeonObject implements Serializable {
    public Energy energy;

    public int health;
    public int maxHealth;
    public int dexterity;
    public int defense;

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
        public boolean friendly; // friendly to the player
        public int speed;

        public Params(int maxHealth, int dexterity, int defense, boolean friendly, int speed) {
            this.maxHealth = maxHealth;
            this.dexterity = dexterity;
            this.defense = defense;
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
        this.friendly = actorParams.friendly;
        this.speed = actorParams.speed;
    }
}
