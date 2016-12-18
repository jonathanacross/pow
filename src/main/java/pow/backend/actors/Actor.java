package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.dungeon.DungeonObject;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.List;

public abstract class Actor extends DungeonObject implements Serializable {
    public boolean needsInput;
    public Energy energy;

    public int health;
    public int maxHealth;
    public boolean friendly; // friendly to the player

    public abstract List<GameEvent> act(GameBackend backend);

    public abstract String getPronoun();

    public Actor(String id, String name, String image, String description, int x, int y, boolean solid, boolean needsInput,
                 int maxHealth, boolean friendly) {
        super(id, name, image, description, x, y, solid);
        this.needsInput = needsInput;
        this.energy = new Energy();
        this.health = maxHealth;
        this.maxHealth = maxHealth;
        this.friendly = friendly;
    }
}
