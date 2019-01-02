package pow.backend.actors.ai;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.util.Metric;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

// class to summarize a map in a form useful for AI
public class AiMap {
    public final double[][] squareWeights;  // smaller weight is better.

    // bounds of the array we fill in above.
    private final int xMin;
    private final int yMin;
    private final int xMax;
    private final int yMax;

    // Points of interest
    public final Point actorLoc;  // location of actor
    public final List<Point> monsterLocs;  // locations of monsters
    public final List<Point> partyLocs;  // locations of other party members
    public final List<Point> unseenLocs;  // locations of unseen squares.

    private static final double TRAP = 200;
    private static final double IMPASSABLE = 1e10;

    // Danger by distance.
    // 0 distance means we have to kill the monster.
    // Within 1 monsters can directly attack; within 3 monsters can wake up.
    // Within 7 monsters might hit with arrows/spells.
    private static final double[][] DANGER_WEIGHTS =
        // distance   0   1  2  3  4  5  6  7
                   {{ 2,  1, 0, 0, 0, 0, 0, 0}, // SAFE
                    { 4,  2, 1, 1, 0, 0, 0, 0}, // NORMAL
                    { 8,  4, 2, 2, 1, 1, 1, 1}, // UNSAFE
                    {16,  8, 4, 4, 2, 2, 2, 2}, // DANGEROUS
                    {32, 16, 8, 8, 4, 4, 4, 4}}; // DEADLY
    private static final double FRIENDLY_BLOCK = 500;

    public boolean canMoveTo(Point p) {
        return xMin <= p.x && p.x < xMax && yMin <= p.y && p.y < yMax &&
                squareWeights[p.x][p.y] < IMPASSABLE;
    }

    private void addDanger(Actor actor, Actor target) {
        if (target.friendly == actor.friendly) {
            squareWeights[target.loc.x][target.loc.y] += FRIENDLY_BLOCK;
            return;
        }

        MonsterDanger.Danger danger = MonsterDanger.getDanger(actor, target);
        int dangerIdx = 0;
        switch (danger) {
            case SAFE: dangerIdx = 0; break;
            case NORMAL: dangerIdx = 1; break;
            case UNSAFE: dangerIdx = 2; break;
            case DANGEROUS: dangerIdx = 3; break;
            case DEADLY: dangerIdx = 4; break;
        }

        for (int dx = -7; dx <= 7; dx++) {
            for (int dy = -7; dy <= 7; dy++) {
                int dist = (int) Metric.rogueMetric.dist(dx, dy);
                int x = target.loc.x + dx;
                int y = target.loc.y + dy;
                if (xMin <= x && x < xMax && yMin <= y && y < yMax) {
                    squareWeights[x][y] += DANGER_WEIGHTS[dangerIdx][dist];
                }
            }
        }
    }

    public AiMap(Actor actor, GameState gs, int radius) {
        GameMap map = gs.getCurrentMap();

        squareWeights = new double[map.width][map.height];

        xMin = Math.max(0, actor.loc.x - radius);
        yMin = Math.max(0, actor.loc.y - radius);
        xMax = Math.min(gs.getCurrentMap().width, actor.loc.x + radius + 1);
        yMax = Math.min(gs.getCurrentMap().height, actor.loc.y + radius + 1);

        actorLoc = new Point(actor.loc.x, actor.loc.y);

        // fill in baseline weights (impassability/traps)
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                if (map.isTerrainBlocked(actor, x, y)) {
                    squareWeights[x][y] = IMPASSABLE;
                } else if (map.hasTrapAt(x, y) && actor.canSeeTraps()) {
                    squareWeights[x][y] = TRAP;
                } else {
                    squareWeights[x][y] = 0;
                }
            }
        }

        // fill in unseen squares
        unseenLocs = new ArrayList<>();
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                if (!map.map[x][y].seen) {
                    unseenLocs.add(new Point(x, y));
                }
            }
        }

        // fill in monsters
        monsterLocs = new ArrayList<>();
        partyLocs = new ArrayList<>();
        for (Actor a : map.actors) {
            if (xMin <= a.loc.x && a.loc.x < xMax &&
                    yMin <= a.loc.y && a.loc.y < yMax) {
                if (!a.friendly) {
                    monsterLocs.add(new Point(a.loc.x, a.loc.y));
                } else if (gs.party.containsActor(a) && !a.loc.equals(actor.loc)) {
                    partyLocs.add(new Point(a.loc.x, a.loc.y));
                }
                addDanger(actor, a);
            }
        }
    }
}
