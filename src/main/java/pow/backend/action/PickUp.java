package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.event.GameEvent;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class PickUp implements Action {
    private final Actor actor;
    private final int itemNum;
    private int numToAdd;

    public PickUp(Actor actor, int itemNum, int numToAdd) {
        this.actor = actor;
        this.itemNum = itemNum;
        this.numToAdd = numToAdd;
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());

        DungeonSquare square = gs.getCurrentMap().map[actor.loc.x][actor.loc.y];
        if (square.items.size() == 0) {
            backend.logMessage(actor.getPronoun() + " can't pick up anything here.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.Failed(null);
        }

        DungeonItem item = square.items.items.get(itemNum);
        // special case for money
        if (item.flags.money) {
            actor.gold += item.count;
            square.items.items.remove(itemNum);
            backend.logMessage(actor.getPronoun() + " pick up " + TextUtils.format(item.name, numToAdd, true),
                    MessageLog.MessageType.GENERAL);
            return ActionResult.Succeeded(events);
        }

        // special case for artifacts
        if (item.artifactSlot != DungeonItem.ArtifactSlot.NONE) {
            if (actor != gs.party.player) {
                // don't let another monster pick up key game items. :)
                return ActionResult.Failed(null);
            }
            // at this point we know that it's the player picking up the artifact
            events.addAll(gs.party.addArtifact(item));
            square.items.items.remove(itemNum);
            // slight hack here.. update the visibility of the game map,
            // since picking up some artifacts may involve lanterns
            gs.getCurrentMap().updatePlayerVisibilityData(gs.party.player, gs.party.pet);
            backend.logMessage(actor.getPronoun() + " pick up " + TextUtils.format(item.name, numToAdd, true),
                    MessageLog.MessageType.GAME_EVENT);
            // Print the description too, so that the player knows what the artifact does.
            backend.logMessage(item.description, MessageLog.MessageType.GAME_EVENT);

            // log if the player won the game
            for (GameEvent event : events) {
                if (event.eventType.equals(GameEvent.EventType.WON_GAME)) {
                    backend.logMessage("Congratulations, you won!", MessageLog.MessageType.GAME_EVENT);
                }
                if (event.eventType.equals(GameEvent.EventType.GOT_PET)) {
                    backend.logMessage("The pet statue glows as you pick it up.", MessageLog.MessageType.GAME_EVENT);
                }
            }
            return ActionResult.Succeeded(events);
        }

        int numCanGet = Math.min(actor.inventory.numCanAdd(item), item.count);
        if (numCanGet <= 0) {
            backend.logMessage(actor.getPronoun() + " can't hold any more.", MessageLog.MessageType.USER_ERROR);
            return ActionResult.Failed(null);
        }

        numToAdd = Math.min(numToAdd, numCanGet); // make sure we don't add more than we are able!
        if (numToAdd == item.count) {
            // if can pick up all, then just transfer the item to inventory
            actor.inventory.add(item);
            square.items.items.remove(itemNum);
        } else {
            // if can just pick up some, then have to clone object, and update counts
            DungeonItem cloneForInventory = new DungeonItem(item);
            cloneForInventory.count = numToAdd;
            item.count -= numToAdd;
            actor.inventory.add(cloneForInventory);
        }
        backend.logMessage(actor.getPronoun() + " pick up " + TextUtils.format(item.name, numToAdd, true),
                MessageLog.MessageType.GENERAL);
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
