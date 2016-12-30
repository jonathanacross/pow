package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.dungeon.DungeonObject;

import java.io.Serializable;

public abstract class Actor extends DungeonObject implements Serializable {
    public Energy energy;

    public int health;
    public int maxHealth;
    public boolean friendly; // friendly to the player
    public int speed;

    public abstract Action act(GameBackend backend);

    public abstract boolean needsInput();

    public abstract String getPronoun();

    public void takeDamage(GameBackend backend, int damage) {
        this.health -= damage;
    }

    public Actor(String id, String name, String image, String description, int x, int y, boolean solid,
                 int maxHealth, boolean friendly, int speed) {
        super(id, name, image, description, x, y, solid);
        this.energy = new Energy();
        this.health = maxHealth;
        this.maxHealth = maxHealth;
        this.friendly = friendly;
        this.speed = speed;
    }
}
