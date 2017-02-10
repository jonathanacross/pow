package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class EnterShop implements Action {

    Actor actor;
    ShopData.ShopState state;

    public EnterShop(Actor actor, ShopData.ShopState state) {
        this.actor = actor;
        this.state = state;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        backend.getGameState().getCurrentMap().shopData.state = this.state;
        backend.logMessage("you enter a " + this.state.toString());

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.InStore());

        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() {
        return false;  // have to play with this.
    }

    @Override
    public Actor getActor() {
        return actor;
    }
}
