package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.event.GameEvent;
import pow.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DropEquipment implements Action {
    private final Player player;
    private final int itemNum;

    public DropEquipment(Player player, int itemNum) {
        this.player = player;
        this.itemNum = itemNum;
    }

    @Override
    public Actor getActor() {
        return this.player;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DUNGEON_UPDATED);

        DungeonSquare square = gs.getCurrentMap().map[player.loc.x][player.loc.y];
        DungeonItem item = player.equipment.items.get(itemNum);
        player.equipment.items.remove(itemNum);
        square.items.add(item);
        backend.logMessage(player.getNoun() + " drops "
                        + TextUtils.formatWithBonus(item.name, item.bonusString(), 1, true),
                MessageLog.MessageType.GENERAL);
        return ActionResult.succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
