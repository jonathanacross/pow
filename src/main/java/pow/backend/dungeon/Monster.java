package pow.backend.dungeon;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Monster extends DungeonObject implements Serializable {

    public Monster(String id, String name, String image, String description, int x, int y) {
        super(id, name, image, description, x, y, true);
    }

    public List<GameEvent> act(GameBackend backend) {
        GameState gs = backend.getGameState();
        int dx = gs.rng.nextInt(3) - 1;
        int dy = gs.rng.nextInt(3) - 1;
        int newx = this.x + dx;
        int newy = this.y + dy;
        List<GameEvent> events = new ArrayList<>();
        if (! gs.map.isBlocked(newx, newy)) {
            if (gs.x == newx && gs.y == newy) {
                backend.logMessage("the " + name + " bumps into you.");
                events.add(GameEvent.ATTACKED);
            } else {
                events.add(GameEvent.MOVED);
                this.x = newx;
                this.y = newy;
            }
        }
        return events;
    }
}
