package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

// Implementations for particular conditions
public class Conditions {

    public static class Health extends Condition implements Serializable {

        public Health(Actor actor) {
            super(actor);
        }

        @Override String getStartMessage() { return actor.getNoun() + " feels vigorous."; }
        @Override String getEndMessage() { return actor.getNoun() + " returns to normal."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feels more vigorous."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feels somewhat less vigorous."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feels the vigor extend."; }

        @Override
        protected List<GameEvent> changeImpl(GameBackend backend, int delta) {
            // If we increase maxHealth, then this will also increase current health.
            // Moreover, if maxHealth decreases, this ensures current health doesn't
            // stay larger than maxHealth.
            actor.increaseHealth(Math.max(delta, 0));
            return Collections.singletonList(GameEventOld.DungeonUpdated());
        }
    }

    public static class Poison extends Condition implements Serializable {
        public Poison(Actor actor) {
            super(actor);
        }

        @Override String getStartMessage() { return(actor.getNoun() + " is poisoned!"); }
        @Override String getEndMessage() { return(actor.getNoun() + " recovers from the poison."); }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feels more poisoned."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feels less poisoned."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feels the poison lasting longer."; }

        @Override
        protected List<GameEvent> updateImpl(GameBackend backend) {
            return actor.takeDamage(backend, getIntensity(), source);
        }
    }

    public static class Stun extends Condition implements Serializable {
        public Stun(Actor actor) { super(actor); }

        @Override String getStartMessage() { return (actor.getNoun() + " is stunned!"); }
        @Override String getEndMessage() { return (actor.getNoun() + " is no longer stunned."); }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feels more stunned."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feels less stunned."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feels stunned longer."; }
    }

    public static class Confuse extends Condition implements Serializable {
        public Confuse(Actor actor) { super(actor); }

        @Override String getStartMessage() { return(actor.getNoun() + " is confused!"); }
        @Override String getEndMessage() { return(actor.getNoun() + " is no longer confused."); }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feels more confused."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feels less confused."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feels confused longer."; }
    }

    public static class Speed extends Condition implements Serializable {
        public Speed(Actor actor) {
            super(actor);
        }

        @Override String getStartMessage() { return actor.getNoun() + " starts moving faster!"; }
        @Override String getEndMessage() { return actor.getNoun() + " returns to normal speed."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " starts moving even faster."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " slows down some."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feels the haste lasting longer."; }
    }

    public static class ToHit extends Condition implements Serializable {
        public ToHit(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " can hit more accurately!"; }
        @Override String getEndMessage() { return actor.getNoun() + "'s accuracy returns to normal."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " can hit even more accurately!"; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " can hit somewhat less accurately."; }
        @Override String getExtendMessage() { return actor.getNoun() + " can hit accurately for a longer time."; }
    }

    public static class ToDam extends Condition implements Serializable {
        public ToDam(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " can hit harder!"; }
        @Override String getEndMessage() { return actor.getNoun() + "'s damage returns to normal."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " can hit even harder!"; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " cannot hit quite as hard."; }
        @Override String getExtendMessage() { return actor.getNoun() + " can hit hard for a longer time."; }
    }

    public static class Defense extends Condition implements Serializable {
        public Defense(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " is harder to hit!"; }
        @Override String getEndMessage() { return actor.getNoun() + "'s defense returns to normal."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " is even harder to be hit!"; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " is a little easier to be hit."; }
        @Override String getExtendMessage() { return actor.getNoun() + " is hard to hit for a longer time."; }
    }

    public static class ResistCold extends Condition implements Serializable {
        public ResistCold(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " is resistant to cold."; }
        @Override String getEndMessage() { return actor.getNoun() + " is susceptible to cold."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " is more resistant to cold."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " is less resistant to cold."; }
        @Override String getExtendMessage() { return actor.getNoun() + " is resistant to cold longer."; }
    }

    public static class ResistFire extends Condition implements Serializable {
        public ResistFire(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " is resistant to fire."; }
        @Override String getEndMessage() { return actor.getNoun() + " is susceptible to fire."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " is more resistant to fire."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " is less resistant to fire."; }
        @Override String getExtendMessage() { return actor.getNoun() + " is resistant to fire longer."; }
    }

    public static class ResistAcid extends Condition implements Serializable {
        public ResistAcid(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " is resistant to acid."; }
        @Override String getEndMessage() { return actor.getNoun() + " is susceptible to acid."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " is more resistant to acid."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " is less resistant to acid."; }
        @Override String getExtendMessage() { return actor.getNoun() + " is resistant to acid longer."; }
    }

    public static class ResistPoison extends Condition implements Serializable {
        public ResistPoison(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " is resistant to poison."; }
        @Override String getEndMessage() { return actor.getNoun() + " is susceptible to poison."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " is more resistant to poison."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " is less resistant to poison."; }
        @Override String getExtendMessage() { return actor.getNoun() + " is resistant to poison longer."; }
    }

    public static class ResistElectricity extends Condition implements Serializable {
        public ResistElectricity(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " is resistant to electricity."; }
        @Override String getEndMessage() { return actor.getNoun() + " is susceptible to electricity."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " is more resistant to electricity."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " is less resistant to electricity."; }
        @Override String getExtendMessage() { return actor.getNoun() + " is resistant to electricity longer."; }
    }

    public static class ResistDamage extends Condition implements Serializable {
        public ResistDamage(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " is resistant to damage."; }
        @Override String getEndMessage() { return actor.getNoun() + " is susceptible to damage."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " is more resistant to damage."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " is less resistant to damage."; }
        @Override String getExtendMessage() { return actor.getNoun() + " is resistant to damage longer."; }
    }
}
