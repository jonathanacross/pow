package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.SpellParams;
import pow.backend.action.Action;
import pow.backend.action.Attack;
import pow.backend.action.Move;
import pow.backend.ai.*;
import pow.backend.dungeon.DungeonObject;
import pow.backend.GameConstants;
import pow.backend.event.GameEvent;
import pow.util.Circle;
import pow.util.MathUtils;
import pow.util.Point;
import pow.util.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Monster extends Actor implements Serializable {

    public enum ActorState {
        SLEEPING,
        ATTACKING,
        WANDERING,
        DUMB_AWAKE, // awake, for erratic creatures
        AFRAID
    }

    public static class Flags implements Serializable {
        public final boolean stationary;  // can't move (e.g., a mushroom or mold)
        public final boolean erratic;  // just move randomly, e.g., insects
        public final boolean knight;  // moves like a knight
        public final boolean fearless;  // won't get scared/run away
        public final boolean passive;  // doesn't attack player unless attacked
        public final boolean perfect;  // never have random moves

        public Flags(boolean stationary, boolean erratic, boolean knight,
                     boolean fearless, boolean passive, boolean perfect) {
            this.stationary = stationary;
            this.erratic = erratic;
            this.knight = knight;
            this.fearless = fearless;
            this.passive = passive;
            this.perfect = perfect;
        }
    }

    private int currStateTurnCount;

    private ActorState state;
    private final Flags flags;

    public Monster(DungeonObject.Params objectParams, Actor.Params actorParams, Flags flags) {
        super(objectParams, actorParams);
        this.currStateTurnCount = 0;
        this.state = ActorState.SLEEPING;
        this.flags = flags;
    }

    private void updateState(ActorState newState, GameBackend backend) {
        if (this.state == newState) {
            return;
        }

        switch (newState) {
            case DUMB_AWAKE:
                backend.logMessage(this.getNoun() + " wakes up!", MessageLog.MessageType.GENERAL);
                break;
            case AFRAID:
                // TODO: shouldn't be called for stationary creatures
                backend.logMessage(this.getNoun() + " flees in panic!", MessageLog.MessageType.GENERAL);
                break;
            case ATTACKING:
                backend.logMessage(this.getNoun() + " recovers its courage.", MessageLog.MessageType.GENERAL);
                break;
            // TODO: shouldn't be called for stationary creatures
            case WANDERING:
                backend.logMessage(this.getNoun() + " wanders aimlessly.", MessageLog.MessageType.GENERAL);
                break;
            case SLEEPING:
                backend.logMessage(this.getNoun() + " falls asleep.", MessageLog.MessageType.GENERAL);
                break;
        }
        this.state = newState;
        this.currStateTurnCount = 0;
    }

    @Override
    public List<GameEvent> takeDamage(GameBackend backend, int damage, Actor source) {
        // don't bother changing state if we will die!
        if (damage <= this.getHealth()) {
            if (flags.erratic) {
                updateState(ActorState.DUMB_AWAKE, backend);
            } else {
                if (!flags.fearless && (damage > 0.3 * this.getHealth())) {
                    updateState(ActorState.AFRAID, backend);
                } else {
                    updateState(ActorState.ATTACKING, backend);
                }
            }
        }
        return super.takeDamage(backend, damage, source);
    }

    @Override
    public void putToSleep(GameBackend backend) {
        updateState(ActorState.SLEEPING, backend);
    }

    private List<SpellParams> getCastableSpells(GameState gs, Actor target) {
        List<SpellParams> castableSpells = new ArrayList<>();
        for (SpellParams spell : this.spells) {
            if (SpellAi.shouldMonsterCastSpell(spell, this, gs, target)) {
                castableSpells.add(spell);
            }
        }
        return castableSpells;
    }


    private Action trackTarget(GameState gs, Point target) {
        if (isConfused()) {
            return doConfused(gs);
        }

        if (flags.erratic) {
            // move randomly
            return movement.wander(this, gs);
        }

        if (!flags.perfect && gs.rng.nextInt(8) == 0) {
            return movement.wander(this, gs);
        }

        // try to track the target
        return movement.moveTowardTarget(this, gs, target);
    }

    private Action doConfused(GameState gs) {
        return movement.wander(this, gs);
    }

    private Action doWander(GameBackend backend) {
        // if there's something nearby, go after it!
        GameState gs = backend.getGameState();
        if (!flags.passive) {
            Actor closestEnemy = movement.findNearestEnemy(this, gs);
            if (AiUtils.enemyIsWithinRange(this, closestEnemy, 15)) {
                updateState(ActorState.ATTACKING, backend);
                return doAttack(backend);
            }
        }

        // fall asleep if bored
        if (currStateTurnCount >= 20) {
            updateState(ActorState.SLEEPING, backend);
            return doSleep(backend);
        }

        return movement.wander(this, gs);
    }

    private Action castSpell(List<SpellParams> castableSpells, Actor closestEnemy, GameState gs) {
        int spellIdx = gs.rng.nextInt(castableSpells.size());
        return SpellParams.buildAction(castableSpells.get(spellIdx), this, closestEnemy.loc);
    }

    private Action doDumbAwake(GameBackend backend) {
        GameState gs = backend.getGameState();

        if (isConfused()) {
            return doConfused(gs);
        }

        Actor closestEnemy = movement.findNearestEnemy(this, gs);

        // if nothing nearby, then fall asleep
        if (! AiUtils.enemyIsWithinRange(this, closestEnemy, 15)) {
            updateState(ActorState.SLEEPING, backend);
            return doSleep(backend);
        }

        // attack if possible
        if (closestEnemy != null && movement.canHit(this, closestEnemy)) {
            return new Attack(this, closestEnemy);
        }

        // cast a spell if possible
        List<SpellParams> castableSpells = getCastableSpells(gs, closestEnemy);
        if (!castableSpells.isEmpty() && gs.rng.nextInt(2) == 0) {
            return castSpell(castableSpells, closestEnemy, gs);
        }

        // wander aimlessly
        return movement.wander(this, gs);
    }

    private Action doAttack(GameBackend backend) {
        GameState gs = backend.getGameState();

        if (isConfused()) {
            return doConfused(gs);
        }

        Actor closestEnemy = movement.findNearestEnemy(this, gs);

        // if nothing nearby, then just wander
        if (! AiUtils.enemyIsWithinRange(this, closestEnemy, 15)) {
            updateState(ActorState.WANDERING, backend);
            return doWander(backend);
        }

        // attack if possible
        if (movement.canHit(this, closestEnemy)) {
            return new Attack(this, closestEnemy);
        }

        // cast a spell if possible
        List<SpellParams> castableSpells = getCastableSpells(gs, closestEnemy);
        if (!castableSpells.isEmpty() && gs.rng.nextInt(2) == 0) {
            return castSpell(castableSpells, closestEnemy, gs);
        }

        // try to track enemy
        return trackTarget(gs, closestEnemy.loc);
    }

    private Action doSleep(GameBackend backend) {
        GameState gs = backend.getGameState();

        Actor closestEnemy = movement.findNearestEnemy(this, gs);
        if (AiUtils.enemyIsWithinRange(this, closestEnemy, 3) ||
                AiUtils.enemyIsWithinRange(this, gs.party.player, 3)) {
            // Randomly wake up if near an enemy or the player. (This
            // is two cases, because the player may wake nearby townspeople,
            // but your pet may wake a monster.)
            if (gs.rng.nextInt(4) == 0) {
                if (flags.erratic) {
                    updateState(ActorState.DUMB_AWAKE, backend);
                    return doDumbAwake(backend);
                } else {
                    if ((closestEnemy != null && closestEnemy.friendly == this.friendly)
                            || flags.passive) {
                        updateState(ActorState.WANDERING, backend);
                        return doWander(backend);
                    } else {
                        updateState(ActorState.ATTACKING, backend);
                        return doAttack(backend);
                    }
                }
            }
        }

        // still sleeping -- don't do anything
        return new Move(this, 0, 0);
    }

    private Action doAfraid(GameBackend backend) {
        GameState gs = backend.getGameState();

        if (isConfused()) {
            return doConfused(gs);
        }

        Actor closestEnemy = movement.findNearestEnemy(this, gs);

        // if nothing nearby, then stop being afraid.
        if (! AiUtils.enemyIsWithinRange(this, closestEnemy, 15)) {
            updateState(ActorState.WANDERING, backend);
            return doWander(backend);
        }

        // try to escape; move away from closest target
        Point escapeTarget = new Point(2*loc.x - closestEnemy.loc.x, 2*loc.y - closestEnemy.loc.y);
        if (movement.canMoveTowardTarget(this, gs, escapeTarget)) {
           return movement.moveTowardTarget(this, gs, escapeTarget);
        }

        // obvious escape route is blocked, so try to attack
        // attack if possible
        if (movement.canHit(this, closestEnemy)) {
            return new Attack(this, closestEnemy);
        }

        // cast a spell if possible
        List<SpellParams> castableSpells = getCastableSpells(gs, closestEnemy);
        if (!castableSpells.isEmpty() && gs.rng.nextInt(2) == 0) {
            return castSpell(castableSpells, closestEnemy, gs);
        }

        // can't flee, can't attack? run around randomly.
        return movement.wander(this, gs);
    }

    @Override
    public Action act(GameBackend backend) {
        currStateTurnCount++;
        switch (state) {
            case SLEEPING: return doSleep(backend);
            case WANDERING: return doWander(backend);
            case ATTACKING: return doAttack(backend);
            case AFRAID: return doAfraid(backend);
            case DUMB_AWAKE: return doDumbAwake(backend);
        }
        return null;
    }

    @Override
    public boolean canSeeLocation(GameState gs, Point point) {
        // must be on the map, and within the view radius.  Monsters can see unlit squares.
        return (gs.getCurrentMap().isOnMap(point.x, point.y) &&
                (MathUtils.dist2(loc, point) <= Circle.getRadiusSquared(GameConstants.MONSTER_VIEW_RADIUS)));
    }

    @Override
    public String getNoun() {
        return TextUtils.format(this.name, 1, true);
    }

    @Override
    public boolean needsInput(GameState gameState) {
        return false;
    }
}
