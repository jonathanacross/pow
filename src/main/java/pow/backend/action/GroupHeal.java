package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.MessageLog;
import pow.backend.Party;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class GroupHeal implements Action {
    private final Actor actor;
    private final int amount;

    public GroupHeal(Actor actor, int amount) {
        this.actor = actor;
        this.amount = amount;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.Healed());

        Party party = backend.getGameState().party;
        healOne(party.player, this.amount, backend);
        if (party.pet != null) {
            healOne(party.pet, this.amount, backend);
        }
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }

    private void healOne(Player player, int amount, GameBackend backend) {
        int desiredHealAmount = Math.max((int)Math.round(actor.getMaxHealth() * 0.01 * amount), amount);
        int healAmount = player.increaseHealth(desiredHealAmount);
        backend.logMessage(player.getPronoun() + " healed " + healAmount, MessageLog.MessageType.GENERAL);
    }
}
