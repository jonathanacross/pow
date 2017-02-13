package pow.backend.actors;

import pow.backend.GameState;
import pow.backend.dungeon.DungeonObject;

import java.io.Serializable;

public class Monster extends AiActor implements Serializable {

    public Monster(DungeonObject.Params objectParams, Actor.Params actorParams, Flags flags) {
        super(objectParams, actorParams, flags);
    }

    @Override
    public String getPronoun() {
        return "the " + this.name;
    }

    @Override
    public boolean needsInput(GameState gameState) {
        return false;
    }
}
