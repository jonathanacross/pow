package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.ShopData;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class RestAtInn implements Action {

    @Override
    public ActionResult process(GameBackend backend) {
        Player player = backend.getGameState().party.player;
        Player pet = backend.getGameState().party.pet;
        ShopData shopData = backend.getGameState().getCurrentMap().shopData;
        shopData.state = ShopData.ShopState.NONE;

        if (player.gold >= shopData.innCost) {
            player.gold -= shopData.innCost;
            player.setFullHealth();
            player.setFullMana();
            if (pet != null) {
                pet.setFullHealth();
                pet.setFullMana();
            }
            backend.logMessage(player.getNoun() + " feels refreshed.", MessageLog.MessageType.GENERAL);
            List<GameEvent> events = new ArrayList<>();

            // save the game!
            backend.tellSelectedActor(new Save());

            events.add(GameEvent.DUNGEON_UPDATED);
            return ActionResult.succeeded(events);
        } else {
            backend.logMessage(player.getNoun() + " does not have enough money.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.failed();
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
