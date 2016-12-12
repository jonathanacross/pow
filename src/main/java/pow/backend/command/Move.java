package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.event.GameEvent;

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
        DungeonFeature feature = gs.map.map[gs.x][gs.y].feature;
        if (feature != null && feature.name.equals("orange pearl")) {
            backend.logMessage("you won!");
            events.add(GameEvent.WON_GAME);
        } else if (feature != null && feature.name.equals("cobra")) {
            backend.logMessage("you died.");
            events.add(GameEvent.LOST_GAME);
        }
        return events;
    }

    @Override
    public List<GameEvent> process(GameBackend backend) {
        GameState gs = backend.getGameState();
        int newx = gs.x + dx;
        int newy = gs.y + dy;
        if (! gs.map.map[newx][newy].blockGround()) {
            gs.x = newx;
            gs.y = newy;
        }

        return addEvents(backend);
    }
}