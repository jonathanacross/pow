package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.command.Attack;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Monster extends Actor implements Serializable {

    public Monster(String id, String name, String image, String description, int maxHealth, int x, int y) {
        super(id, name, image, description, x, y, true, false, maxHealth, false);
    }

    public static Monster makeRat(int x, int y) {
        return new Monster("white rat", "white rat", "white rat", "white rat", 3, x, y);
    }

    public static Monster makeBat(int x, int y) {
        return new Monster("bat", "bat", "bat", "bat", 2, x, y);
    }

    public static Monster makeSnake(int x, int y) {
        return new Monster("yellow snake", "yellow snake", "yellow snake", "yellow snake", 4, x, y);
    }

    public String getPronoun() {
        return "the " + this.name;
    }

    // TODO: duplicate code; factor out point and math utils
    private int dist2(int x1, int y1, int x2, int y2) {
        return (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2);
    }

    // TODO: pull out in helper routine, to share between all actors
    private Actor nearestActor(GameState gs) {
        int bestDist = Integer.MAX_VALUE;
        Actor closestMonster = null;
        for (Actor m : gs.map.actors) {
            if (m.friendly) {
                int d2 = dist2(x, y, m.x, m.y);
                if (closestMonster == null || d2 < bestDist) {
                    closestMonster = m;
                    bestDist = d2;
                }
            }
        }
        return closestMonster;
    }

    public List<GameEvent> act(GameBackend backend) {
        GameState gs = backend.getGameState();

        // try to attack first
        Actor closestEnemy = nearestActor(gs);
        if (closestEnemy != null && dist2(x, y, closestEnemy.x, closestEnemy.y) <= 2) {
            return Attack.doAttack(backend, this, closestEnemy);
        }

        // move randomly
        int dx = gs.rng.nextInt(3) - 1;
        int dy = gs.rng.nextInt(3) - 1;
        int newx = this.x + dx;
        int newy = this.y + dy;
        List<GameEvent> events = new ArrayList<>();
        if (!gs.map.isBlocked(newx, newy)) {
            events.add(GameEvent.MOVED);
            this.x = newx;
            this.y = newy;
        }
        return events;
    }
}
