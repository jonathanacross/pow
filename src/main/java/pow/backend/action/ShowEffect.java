package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;

public class ShowEffect implements Action {

    private final DungeonEffect effect;

    public ShowEffect(DungeonEffect effect) {
        this.effect = effect;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        backend.getGameState().getCurrentMap().effects.clear();
        backend.getGameState().getCurrentMap().effects.add(this.effect);
        return ActionResult.succeeded(GameEvent.EFFECT);
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public Actor getActor() {
        return null;
    }
}
