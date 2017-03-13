package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.ShopData;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class RestAtInn implements Action {

    @Override
    public ActionResult process(GameBackend backend) {
        Player player = backend.getGameState().player;
        ShopData shopData = backend.getGameState().getCurrentMap().shopData;
        shopData.state = ShopData.ShopState.NONE;

        if (player.gold >= shopData.innCost) {
            player.gold -= shopData.innCost;
            player.setFullHealth();
            player.setFullMana();
            backend.logMessage("You feel refreshed.");
            List<GameEvent> events = new ArrayList<>();
            events.add(GameEvent.DungeonUpdated());
            return ActionResult.Succeeded(events);
        } else {
            backend.logMessage("You do not have enough money.");
            return ActionResult.Failed(null);
        }
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
