package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Attack;
import pow.backend.action.Move;
import pow.util.MathUtils;

import java.io.Serializable;

// TODO: see if it makes sense to have a separate class for this (do pets need it?)
// otherwise, merge this logic directly into the monster class.
public abstract class AiActor extends Actor {

    public enum ActorState {
        SLEEPING,
        AWAKE   // eventually, replace this with more fine-grained states: WANDERING, FLEEING, ATTACKING
    }

    public static class Flags implements Serializable {
        public boolean stationary;  // can't move (e.g., a mushroom or mold)
        public boolean erratic;  // just move randomly, e.g., insects
//        public boolean aggressive;  // won't get scared/run away
//        public boolean passive;  // doesn't attack player unless attacked
//        public boolean perfect;  // never have random moves
//        public boolean knight;  // moves like a knight

        public Flags(boolean stationary, boolean erratic) {
            this.stationary = stationary;
            this.erratic = erratic;
        }
    }

    protected int stateTurnCount; // how long have we been in this state?
    protected ActorState state;
    protected Flags flags;

    public AiActor(String id, String name, String image, String description, int x, int y, boolean solid, int maxHealth, boolean friendly, int speed, Flags flags) {
        super(id, name, image, description, x, y, solid, maxHealth, friendly, speed);
        stateTurnCount = 0;
        state = ActorState.SLEEPING;
        this.flags = flags;
    }

    @Override
    public void takeDamage(GameBackend backend, int damage) {
        super.takeDamage(backend, damage);
        if (this.state == ActorState.SLEEPING) {
            backend.logMessage("the " + this.name + " wakes up!");
            this.state = ActorState.AWAKE;
        }
    }

    private Action doAwake(GameBackend backend) {
        GameState gs = backend.getGameState();
        // attack if adjacent to an enemy
        Actor closestEnemy = AiUtils.findNearestTarget(this, gs);
        if (closestEnemy != null && MathUtils.dist2(loc, closestEnemy.loc) <= 2) {
            return new Attack(this, closestEnemy);
        }

        if (flags.stationary) {
            return new Move(this, 0, 0);
        }

        if (flags.erratic) {
            // move randomly
            return AiUtils.wander(this, gs);
        }

        // try to track the player
        return AiUtils.moveTowardTarget(this, gs, gs.player.loc);
    }

    private Action doSleep(GameBackend backend) {
        GameState gs = backend.getGameState();
        Actor closestEnemy = AiUtils.findNearestTarget(this, gs);
        if (closestEnemy != null && MathUtils.dist2(loc, closestEnemy.loc) <= 4) {
            // randomly wake up if near an enemy
            if (gs.rng.nextInt(4) == 0) {
                backend.logMessage("the " + this.name + " wakes up!");
                this.state = ActorState.AWAKE;
                return doAwake(backend);
            }
        }

        // still sleeping -- don't do anything
        return new Move(this, 0, 0);
    }

    @Override
    public Action act(GameBackend backend) {
        switch (state) {
            case SLEEPING: return doSleep(backend);
            case AWAKE: return doAwake(backend);
            default: return null;
        }
    }
}
