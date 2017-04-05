package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.action.Attack;
import pow.backend.action.Move;
import pow.backend.action.spell.Arrow;
import pow.backend.actors.ai.KnightAi;
import pow.backend.dungeon.DungeonObject;
import pow.backend.event.GameEvent;
import pow.util.MathUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Monster extends Actor implements Serializable {

    public enum ActorState {
        SLEEPING,
        AWAKE   // eventually, replace this with more fine-grained states: WANDERING, FLEEING, ATTACKING
    }

    public static class Flags implements Serializable {
        public final boolean stationary;  // can't move (e.g., a mushroom or mold)
        public final boolean erratic;  // just move randomly, e.g., insects
        public final boolean knight;  // moves like a knight
//        public final boolean aggressive;  // won't get scared/run away
//        public final boolean passive;  // doesn't attack player unless attacked
//        public final boolean perfect;  // never have random moves

        public Flags(boolean stationary, boolean erratic, boolean knight) {
            this.stationary = stationary;
            this.erratic = erratic;
            this.knight = knight;
        }
    }


    private final int stateTurnCount; // how long have we been in this state?

    private ActorState state;
    private final Flags flags;

    public Monster(DungeonObject.Params objectParams, Actor.Params actorParams, Flags flags) {
        super(objectParams, actorParams);
        this.stateTurnCount = 0;
        this.state = ActorState.SLEEPING;
        this.flags = flags;

        this.baseStats.speed = actorParams.speed;
    }

    @Override
    public List<GameEvent> takeDamage(GameBackend backend, int damage) {
        if (this.state == ActorState.SLEEPING) {
            backend.logMessage("the " + this.name + " wakes up!");
            this.state = ActorState.AWAKE;
        }
        return super.takeDamage(backend, damage);
    }

    private List<Monster.Spell> getCastableSpells() {
        List<Monster.Spell> castableSpells = new ArrayList<>();
        for (Monster.Spell spell : this.spells) {
            if (spell.getRequiredMana() <= this.getMana()) {
                castableSpells.add(spell);
            }
        }
        return castableSpells;
    }

    private Action doAwake(GameBackend backend) {
        GameState gs = backend.getGameState();

        // attack if possible
        Actor closestEnemy = flags.knight ?
                KnightAi.findNearestTargetKnight(this, gs) :
                AiUtils.findNearestTarget(this, gs);
        if (closestEnemy != null) {
            int dist2 = MathUtils.dist2(loc, closestEnemy.loc);
            boolean canHit = flags.knight ? dist2 == 5 : dist2 <= 2;
            if (canHit) {
                return new Attack(this, closestEnemy);
            }
        }

        // cast a spell if possible
        if (closestEnemy != null) {
            List<Monster.Spell> castableSpells = getCastableSpells();
            if (!castableSpells.isEmpty()) {
                int spellIdx = gs.rng.nextInt(castableSpells.size());
                switch (castableSpells.get(spellIdx)) {
                    case ARROW:
                        return new Arrow(this, closestEnemy.loc);
                }
            }
        }

        if (flags.stationary) {
            return new Move(this, 0, 0);
        }

        if (flags.erratic) {
            // move randomly
            return AiUtils.wander(this, gs);
        }

        // try to track the player
        return flags.knight ?
                KnightAi.knightMoveTowardTarget(this, gs, gs.player.loc) :
                AiUtils.moveTowardTarget(this, gs, gs.player.loc);
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

    @Override
    public String getPronoun() {
        return "the " + this.name;
    }

    @Override
    public boolean needsInput(GameState gameState) {
        return false;
    }
}
