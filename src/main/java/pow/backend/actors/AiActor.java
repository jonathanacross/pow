package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Attack;
import pow.backend.action.Move;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.gen.ArtifactData;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.event.GameEvent;
import pow.util.MathUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    private int stateTurnCount; // how long have we been in this state?
    private int baseSpeed;

    @Override protected int getBaseSpeed() { return baseSpeed; }
    private ActorState state;
    private Flags flags;

    public AiActor(DungeonObject.Params objectParams, Actor.Params actorParams, Flags flags) {
        super(objectParams, actorParams);
        this.stateTurnCount = 0;
        this.state = ActorState.SLEEPING;
        this.flags = flags;

        this.baseSpeed = actorParams.speed;
    }

    @Override
    public List<GameEvent> takeDamage(GameBackend backend, int damage) {
        if (this.state == ActorState.SLEEPING) {
            backend.logMessage("the " + this.name + " wakes up!");
            this.state = ActorState.AWAKE;
        }
        return super.takeDamage(backend, damage);
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
