package pow.backend.actors.ai;

import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.actors.Actor;
import pow.util.Point;

public interface Movement {

    // Returns a random legal move (may include staying in the same position.
    Action wander(Actor actor, GameState gs);

    // Returns an action for the actor to go toward the target
    // (staying in the same position if not possible).
    Action moveTowardTarget(Actor actor, GameState gs, Point target);

    // Returns true if the actor is able to go toward the target.
    boolean canMoveTowardTarget(Actor actor, GameState gs, Point target);

    // Finds the 'nearest' target to the actor (given the actor's alignment
    // and movement type).  May return null if there is no nearest actor.
    Actor findNearestTarget(Actor actor, GameState gs);

    // Returns true if the actor can hit the target.
    boolean canHit(Actor actor, Actor target);
}
