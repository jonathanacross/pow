package pow.backend.behavior;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.MoveRequest;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonFeature;
import pow.util.Direction;
import pow.util.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunBehavior implements Behavior, Serializable {

    private final Actor actor;
    private Direction direction;
    private int stepCount;
    private boolean openLeft;
    private boolean openRight;
    private int initialHealth;

    public RunBehavior(Actor actor, Direction direction) {
        this.actor = actor;
        this.direction = direction;
        this.stepCount = 0;
        this.initialHealth = actor.health;
    }

    @Override
    public boolean canPerform(GameState gs) {
        if (stepCount == 0) {
            // On the first step, always allow the actor to
            // try to move this direction; this allows the actor
            // to open doors, dig, change levels, etc., even if
            // they have the Shift key down for running.
            return true;
        }

        // Check to see if we've lost health. If we're hit, stop running!
        if (actor.health < initialHealth) {
            return false;
        }

        // on the second step, figure out if we're in a corridor and
        // which way it's going.
        if (stepCount == 1) {

            // Get a list of the open directions nearby to the direction of
            // interest. If the actor is running straight (NSEW), allow up to a 90
            // degree turn. This covers cases like:
            //
            //     ####
            //     .@.#
            //     ##.#
            //
            // If the actor presses right here, we want to take a first step, then
            // turn and run south. If the actor is running diagonally, we only allow
            // a 45 degree turn. That way it doesn't get confused by cases like:
            //
            //      #.#
            //     ##.##
            //     .@...
            //     #####
            //
            // If the actor presses NE here, we want to run north and not get
            // confused by the east passage.
            boolean include90Degrees = (direction == Direction.N || direction == Direction.E ||
                    direction == Direction.S || direction == Direction.W);
            List<Direction> openDirs = getOpenDirs(gs, include90Degrees);

            if (openDirs.isEmpty()) return false;

            if (openDirs.size() == 1) {
                // Entering a corridor.
                openLeft = false;
                openRight = false;

                // The direction may change if the first step entered a corridor from
                // around a corner.
                direction = openDirs.get(0);
            } else {
                // Entering an open area.
                openLeft = isDirectionOpen(gs.getCurrentMap(), actor.loc, direction.rotateLeft90);
                openRight = isDirectionOpen(gs.getCurrentMap(), actor.loc, direction.rotateRight90);
            }
        } else {  // >= 2 steps
            // sanity check in case we have some running bug or actor is in some pathological path.
            if (stepCount > 500) return false;

            if (!openLeft && !openRight) {
                if (!runInCorridor(gs)) return false;
            } else {
                if (!runInOpen(gs)) return false;
            }
        }

        return canKeepRunning(gs) && !seeInterestingThings(gs);
    }

    @Override
    public Action getAction() {
        stepCount++;
        return new MoveRequest(actor, direction.dx, direction.dy, true);
    }

    // See if the actor can take one step while in a corridor.
    //
    // The actor will follow curves and turns as long as there is only one
    // direction they can go. (This is more or less true, though right-angle
    // turns need special handling.)
    private boolean runInCorridor(GameState gs) {
        // Keep running as long as there's only one direction to go. Allow up to a
        // 90 degree turn while running.
        List<Direction> openDirs = getOpenDirs(gs, true);

        if (openDirs.size() == 1) {
            direction = openDirs.get(0);
            return true;
        }

        // If we're approaching a right-angle turn, keep going. We'd normally
        // stop here because there are two ways you can go, straight into the
        // corner of the turn (1) or diagonal to take a shortcut around it (2):
        //
        //     ####
        //     #12.
        //     #@##
        //     #^#
        //
        // We detect this case by seeing if there are two (and only two) open
        // directions: ahead and 45 degrees *and* if one step past that is blocked.
        if (openDirs.size() != 2) return false;
        if (!openDirs.contains(direction)) return false;
        if (!openDirs.contains(direction.rotateLeft45) &&
                !openDirs.contains(direction.rotateRight45)) return false;
        Point oneStepAhead = actor.loc.add(direction);
        if (isDirectionOpen(gs.getCurrentMap(), oneStepAhead, direction)) return false;

        // If we got here, we're in a corner. Keep going straight.
        return true;
    }

    // See if we can advance a step in the open.
    // Whether or not the actor's left and right sides are open cannot change.
    // In other words, if he is running along a wall on his left (closed on
    // left, open on right), he will stop if he enters an open room (open on
    // both).
    private boolean runInOpen(GameState gs) {
        GameMap map = gs.getCurrentMap();
        boolean nextLeft = isDirectionOpen(map, actor.loc, direction.rotateLeft45);
        boolean nextRight = isDirectionOpen(map, actor.loc, direction.rotateRight45);
        return openLeft == nextLeft && openRight == nextRight;
    }

    private List<Direction> getOpenDirs(GameState gs, boolean include90Degree) {
        List<Direction> dirs = include90Degree ?
                Arrays.asList(
                        direction.rotateLeft90,
                        direction.rotateLeft45,
                        direction,
                        direction.rotateRight45,
                        direction.rotateRight90) :
                Arrays.asList(
                        direction.rotateLeft45,
                        direction,
                        direction.rotateRight45);

        List<Direction> openDirs = new ArrayList<>();
        for (Direction d : dirs) {
            if (isDirectionOpen(gs.getCurrentMap(), actor.loc, d)) {
                openDirs.add(d);
            }
        }

        return openDirs;
    }

    // Returns true if the actor can run one step in the current direction.
    // Returns false if they should stop because they'd hit a wall or enemy actor.
    private boolean canKeepRunning(GameState gs) {
        Point nextLoc = actor.loc.add(direction);
        GameMap map = gs.getCurrentMap();
        return !landOrEnemyBlocked(map, nextLoc);
    }

    // Returns true if there are "interesting" things that should cause
    // the actor to stop running.
    private boolean seeInterestingThings(GameState gs) {
        GameMap map = gs.getCurrentMap();

        Direction[] newAdjacentDirs = {
                direction.rotateLeft90,
                direction.rotateLeft45,
                direction,
                direction.rotateRight45,
                direction.rotateRight90};

        // adjacent squares: must not have any monsters, and shouldn't have interesting stuff
        for (Direction d : newAdjacentDirs) {
            Point adj = actor.loc.add(d);
            if (!map.isOnMap(adj.x, adj.y)) return true;
            if (map.actorAt(adj.x, adj.y) != null) return true;
            DungeonFeature feature = map.map[adj.x][adj.y].feature;
            if (feature != null && feature.flags.interesting) return true;
            if (!map.map[adj.x][adj.y].items.items.isEmpty()) return true;
        }

        // Check squares up to 2 away: should not have any enemy monsters
        Point v1 = new Point(direction.rotateLeft90.dx, direction.rotateLeft90.dy);
        Point v2 = new Point(direction.rotateLeft45.dx, direction.rotateLeft45.dy);
        Point v3 = new Point(direction.dx, direction.dy);
        Point v4 = new Point(direction.rotateRight45.dx, direction.rotateRight45.dy);
        Point v5 = new Point(direction.rotateRight90.dx, direction.rotateRight90.dy);
        Point[] newTwoAway = {
                new Point(2 * v1.x, 2 * v1.y),
                new Point(v1.x + v2.x, v1.y + v2.y),
                new Point(2 * v2.x, 2 * v2.y),
                new Point(v2.x + v3.x, v2.y + v3.y),
                new Point(2 * v3.x, 2 * v3.y),
                new Point(v3.x + v4.x, v3.y + v4.y),
                new Point(2 * v4.x, 2 * v4.y),
                new Point(v4.x + v5.x, v4.y + v5.y),
                new Point(2 * v5.x, 2 * v5.y)
        };
        for (Point dir : newTwoAway) {
            Point nearby = new Point(actor.loc.x + dir.x, actor.loc.y + dir.y);
            if (!map.isOnMap(nearby.x, nearby.y)) continue;
            Actor a = map.actorAt(nearby.x, nearby.y);
            if (a != null && !a.friendly) return true;
        }

        return false;
    }

    private boolean isDirectionOpen(GameMap map, Point point, Direction direction) {
        Point loc = point.add(direction);
        if (!map.isOnMap(loc.x, loc.y)) return false;
        return (!map.isBlocked(actor, loc.x, loc.y));
    }

    private boolean landOrEnemyBlocked(GameMap map, Point loc) {
        if (!map.isOnMap(loc.x, loc.y)) return true;
        if (map.isBlocked(actor, loc.x, loc.y)) return true;
        Actor a = map.actorAt(loc.x, loc.y);
        return (a != null && !a.friendly);
    }
}
