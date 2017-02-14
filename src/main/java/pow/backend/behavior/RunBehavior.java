package pow.backend.behavior;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Move;
import pow.backend.actors.Player;
import pow.util.Point;
import pow.util.Direction;

public class RunBehavior implements Behavior {

    Player player;
    Direction direction;

    // directionIdx should match the indices in DirectionSets.All
    public RunBehavior(Player player, Direction direction) {
        this.player = player;
        this.direction = direction;
    }

    @Override
    public boolean canPerform(GameState gameState) {
        Point nextStep = new Point(gameState.player.loc.x, gameState.player.loc.y);
        nextStep.x += direction.dx;
        nextStep.y += direction.dy;
        return (gameState.getCurrentMap().isOnMap(nextStep.x, nextStep.y) &&
                !gameState.getCurrentMap().isBlocked(nextStep.x, nextStep.y));
    }

    @Override
    public Action getAction() {
        return new Move(player, direction.dx, direction.dy);
    }

//    bool canPerform(Hero hero) {
//        if (firstStep) {
//            // On first step, always try to go in direction player pressed.
//        } else if (openLeft == null) {
//            // On the second step, figure out if we're in a corridor and which way
//            // it's going. If the hero is running straight (NSEW), allow up to a 90°
//            // turn. This covers cases like:
//            //
//            //     ####
//            //     .@.#
//            //     ##.#
//            //
//            // If the player presses right here, we want to take a first step, then
//            // turn and run south. If the hero is running diagonally, we only allow
//            // a 45° turn. That way it doesn't get confused by cases like:
//            //
//            //      #.#
//            //     ##.##
//            //     .@...
//            //     #####
//            //
//            // If the player presses NE here, we want to run north and not get
//            // confused by the east passage.
//            var dirs = [direction.rotateLeft45,
//                    direction,
//                    direction.rotateRight45,
//      ];
//
//            if (Direction.cardinal.contains(direction)) {
//                dirs.add(direction.rotateLeft90);
//                dirs.add(direction.rotateRight90);
//            }
//
//            var openDirs = dirs.where((dir) => _isOpen(hero, dir));
//
//            if (openDirs.isEmpty) return false;
//
//            if (openDirs.length == 1) {
//                // Entering a corridor.
//                openLeft = false;
//                openRight = false;
//
//                // The direction may change if the first step entered a corridor from
//                // around a corner.
//                direction = openDirs.first;
//            } else {
//                // Entering an open area.
//                openLeft = _isOpen(hero, direction.rotateLeft90);
//                openRight = _isOpen(hero, direction.rotateRight90);
//            }
//        } else if (!openLeft && !openRight) {
//            if (!_runInCorridor(hero)) return false;
//        } else {
//            if (!_runInOpen(hero)) return false;
//        }
//
//        return _shouldKeepRunning(hero);
//    }
//
//    Action getAction(Hero hero) {
//        firstStep = false;
//        return new WalkAction(direction, running: true);
//    }
//
//    /// Advance one step while in a corridor.
//    ///
//    /// The hero will follow curves and turns as long as there is only one
//    /// direction they can go. (This is more or less true, though right-angle
//    /// turns need special handling.)
//    bool _runInCorridor(Hero hero) {
//        // Keep running as long as there's only one direction to go. Allow up to a
//        // 90° turn while running.
//        var openDirs = [
//        direction.rotateLeft90,
//                direction.rotateLeft45,
//                direction,
//                direction.rotateRight45,
//                direction.rotateRight90
//    ].where((dir) => _isOpen(hero, dir)).toSet();
//
//        if (openDirs.length == 1) {
//            direction = openDirs.first;
//            return true;
//        }
//
//        // Corner case, literally. If we're approaching a right-angle turn, keep
//        // going. We'd normally stop here because there are two ways you can go,
//        // straight into the corner of the turn (1) or diagonal to take a shortcut
//        // around it (2):
//        //
//        //     ####
//        //     #12.
//        //     #@##
//        //     #^#
//        //
//        // We detect this case by seeing if there are two (and only two) open
//        // directions: ahead and 45° *and* if one step past that is blocked.
//        if (openDirs.length != 2) return false;
//        if (!openDirs.contains(direction)) return false;
//        if (!openDirs.contains(direction.rotateLeft45) &&
//                !openDirs.contains(direction.rotateRight45)) return false;
//
//        var twoStepsAhead = hero.game.stage[hero.pos + direction * 2].isTraversable;
//        if (twoStepsAhead) return false;
//
//        // If we got here, we're in a corner. Keep going straight.
//        return true;
//    }
//
//    bool _runInOpen(Hero hero) {
//        // Whether or not the hero's left and right sides are open cannot change.
//        // In other words, if he is running along a wall on his left (closed on
//        // left, open on right), he will stop if he enters an open room (open on
//        // both).
//        var nextLeft = _isOpen(hero, direction.rotateLeft45);
//        var nextRight = _isOpen(hero, direction.rotateRight45);
//        return openLeft == nextLeft && openRight == nextRight;
//    }
//
//    /// Returns `true` if the hero can run one step in the current direction.
//    ///
//    /// Returns `false` if they should stop because they'd hit a wall or actor.
//    bool _shouldKeepRunning(Hero hero) {
//        var stage = hero.game.stage;
//        var pos = hero.pos + direction;
//        if (!stage[pos].isPassable) return false;
//
//        // Don't run into someone.
//        if (stage.actorAt(pos) != null) return false;
//
//        // Don't run next to someone.
//        if (stage.actorAt(pos + direction.rotateLeft90) != null) return false;
//        if (stage.actorAt(pos + direction.rotateLeft45) != null) return false;
//        if (stage.actorAt(pos + direction) != null) return false;
//        if (stage.actorAt(pos + direction.rotateRight45) != null) return false;
//        if (stage.actorAt(pos + direction.rotateRight90) != null) return false;
//
//        return true;
//    }
//
//    bool _isOpen(Hero hero, Direction dir) =>
//    hero.game.stage[hero.pos + dir].isTraversable;
}
