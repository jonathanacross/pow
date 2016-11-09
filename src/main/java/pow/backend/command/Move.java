package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Move extends CommandRequest {
    int dx;
    int dy;

    public Move(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    private List<GameEvent> checkWinLose(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        GameState gs = backend.getGameState();
        if (gs.map.map[gs.y][gs.x] == 'W') {
            events.add(GameEvent.WON_GAME);
        } else if (gs.map.map[gs.y][gs.x] == 'L') {
            events.add(GameEvent.LOST_GAME);
        }
        return events;
    }

    @Override
    public List<GameEvent> process(GameBackend backend) {
        GameState gs = backend.getGameState();
        int newx = gs.x + dx;
        int newy = gs.y + dy;
        if (gs.map.map[newy][newx] != '#') {
            gs.x = newx;
            gs.y = newy;
        }

        return checkWinLose(backend);
    }
}
