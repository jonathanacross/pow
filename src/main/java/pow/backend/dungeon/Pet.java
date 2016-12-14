package pow.backend.dungeon;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.event.GameEvent;
import pow.util.DebugLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pet extends DungeonObject implements Serializable {

    public enum State {
        FOLLOW_PLAYER,
        ATTACK_NEAREST_MONSTER,
        WANDER
    }

    private State state;

    public Pet(String id, String name, String image, String description, int x, int y) {
        super(id, name, image, description, x, y, true);
        state = State.FOLLOW_PLAYER;
    }

    private List<GameEvent> tryMoveTo(GameState gs, int newx, int newy) {
        List<GameEvent> events = new ArrayList<>();
        if (! gs.map.isBlocked(newx, newy) && (gs.player.x != newx || gs.player.y != newy)) {
            events.add(GameEvent.MOVED);
            this.x = newx;
            this.y = newy;
        }
        return events;
    }

    private int dist2(int x1, int y1, int x2, int y2) {
        return (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2);
    }

    private Monster nearestMonster(GameState gs) {

        int bestDist = Integer.MAX_VALUE;
        Monster closestMonster = null;
        for (Monster m : gs.map.monsters) {
            int d2 = dist2(x, y, m.x, m.y);
            if (closestMonster == null || d2 < bestDist) {
                closestMonster = m;
                bestDist = d2;
            }
        }
        return closestMonster;
    }

    private List<GameEvent> moveTowardTarget(GameState gs, int tx, int ty) {
        // TODO: factor out distance computations
        int d2 = dist2(x, y, tx, ty);

        double dist = Math.sqrt(d2);
        int rdx = tx - this.x;
        int rdy = ty - this.y;
        int dx = (int) Math.round(rdx / dist);
        int dy = (int) Math.round(rdy / dist);
        return tryMoveTo(gs, x + dx, y + dy);
    }

    private List<GameEvent> wander(GameState gs) {
        int dx = gs.rng.nextInt(3) - 1;
        int dy = gs.rng.nextInt(3) - 1;
        return tryMoveTo(gs, x + dx, y + dy);
    }

    public List<GameEvent> act(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        GameState gs = backend.getGameState();
        switch (state) {
            case FOLLOW_PLAYER: {
                int d2 = dist2(x, y, gs.player.x, gs.player.y);
                if (d2 <= 2) {
                    state = State.ATTACK_NEAREST_MONSTER;
                    DebugLogger.info("pet attacking nearest");
                }
                events.addAll(moveTowardTarget(gs, gs.player.x, gs.player.y));
                break;
            }

            case ATTACK_NEAREST_MONSTER:
               Monster nearest = nearestMonster(gs);
                if (nearest == null) {
                    state = State.WANDER;
                    DebugLogger.info("pet wandering");
                } else {
                    int d2 = dist2(x, y, nearest.x, nearest.y);
                    if (d2 <= 2) {
                        backend.logMessage("your pet oozes on the monster");
                        state = State.FOLLOW_PLAYER;
                        DebugLogger.info("pet tracking player");
                    } else {
                        events.addAll(moveTowardTarget(gs, nearest.x, nearest.y));
                    }
                }
                break;

            case WANDER:
                events.addAll(wander(gs));
                break;
        }
        return events;
    }
}
