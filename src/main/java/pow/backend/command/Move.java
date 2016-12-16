package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.event.GameEvent;
import pow.util.DebugLogger;

import java.util.ArrayList;
import java.util.List;

public class Move implements CommandRequest {
    int dx;
    int dy;

    public Move(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    private List<GameEvent> addEvents(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.MOVED);
        GameState gs = backend.getGameState();
        DungeonFeature feature = gs.map.map[gs.player.x][gs.player.y].feature;
        if (feature != null && feature.id.equals("wintile")) {
            backend.logMessage("you won!");
            events.add(GameEvent.WON_GAME);
        } else if (feature != null && feature.id.equals("losetile")) {
            backend.logMessage("you died.");
            events.add(GameEvent.LOST_GAME);
        }
        return events;
    }

    @Override
    public List<GameEvent> process(GameBackend backend) {
        GameState gs = backend.getGameState();
        int newx = gs.player.x + dx;
        int newy = gs.player.y + dy;
        if (! gs.map.isBlocked(newx, newy)) {
            gs.player.x = newx;
            gs.player.y = newy;
        }

        return addEvents(backend);
    }
}
