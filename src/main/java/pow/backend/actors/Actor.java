package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.dungeon.DungeonObject;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.List;

public abstract class Actor extends DungeonObject implements Serializable {
    public boolean needsInput;
    public Energy energy;

    public abstract List<GameEvent> act(GameBackend backend);

    public Actor(String id, String name, String image, String description, int x, int y, boolean solid, boolean needsInput) {
        super(id, name, image, description, x, y, solid);
        this.needsInput = needsInput;
        this.energy = new Energy();
    }
}
