package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

// Implementations for particular conditions
public class Conditions {

    public static class Health extends Condition implements Serializable {

        public Health(Actor actor) {
            super(actor);
        }

        @Override String getStartMessage() {
            return actor.getNoun() + " feel vigorous.";
        }
        @Override String getEndMessage() {
            return actor.getNoun() + " return to normal.";
        }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feel more vigorous."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feel somewhat less vigorous."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel the vigor extend."; }

        @Override
        protected List<GameEvent> changeImpl(GameBackend backend, int delta) {
            // If we increase maxHealth, then this will also increase current health.
            // Moreover, if maxHealth decreases, this ensures current health doesn't
            // stay larger than maxHealth.
            actor.increaseHealth(Math.max(delta, 0));
            return Collections.singletonList(GameEvent.DungeonUpdated());
        }
    }

    public static class Poison extends Condition implements Serializable {
        public Poison(Actor actor) {
            super(actor);
        }

        @Override String getStartMessage() { return(actor.getNoun() + " are poisoned!"); }
        @Override String getEndMessage() { return(actor.getNoun() + " recover from the poison."); }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feel more poisoned."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feel less poisoned."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feels the poison lasting longer."; }

        @Override
        protected List<GameEvent> updateImpl(GameBackend backend) {
            return actor.takeDamage(backend, getIntensity());
        }
    }

    public static class Stun extends Condition implements Serializable {
        public Stun(Actor actor) { super(actor); }

        @Override String getStartMessage() { return (actor.getNoun() + " are stunned!"); }
        @Override String getEndMessage() { return (actor.getNoun() + " are no longer stunned."); }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feel more stunned."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feel less stunned."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel stunned longer."; }
    }

    public static class Confuse extends Condition implements Serializable {
        public Confuse(Actor actor) { super(actor); }

        @Override String getStartMessage() { return(actor.getNoun() + " are confused!"); }
        @Override String getEndMessage() { return(actor.getNoun() + " are no longer confused."); }
        @Override String getIncreaseMessage() { return actor.getNoun() + " feel more confused."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " feel less confused."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel confused longer."; }
    }

    public static class Speed extends Condition implements Serializable {
        public Speed(Actor actor) {
            super(actor);
        }

        @Override String getStartMessage() { return actor.getNoun() + " start moving faster!"; }
        @Override String getEndMessage() { return actor.getNoun() + " return to normal speed."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " start moving even faster."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " slow down some."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feels the haste lasting longer."; }
    }

    public static class ToHit extends Condition implements Serializable {
        public ToHit(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " can hit more accurately!"; }
        @Override String getEndMessage() { return actor.getNoun() + " feel your accuracy return to normal."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " can hit even more accurately!"; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " can hit somewhat less accurately."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your accuracy extend."; }
    }

    public static class ToDam extends Condition implements Serializable {
        public ToDam(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " can hit harder!"; }
        @Override String getEndMessage() { return actor.getNoun() + " feel your damage return to normal."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " can hit even harder!"; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " can hit somewhat less hard."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your damage extend."; }
    }

    public static class Defense extends Condition implements Serializable {
        public Defense(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " are harder to hit!"; }
        @Override String getEndMessage() { return actor.getNoun() + " defense returns to normal."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " even harder to hit!"; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " defense decreases somewhat."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your defense extend."; }
    }

    public static class ResistCold extends Condition implements Serializable {
        public ResistCold(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " are resistant to cold."; }
        @Override String getEndMessage() { return actor.getNoun() + " are susceptible to cold."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " are more resistant to cold."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " are less resistant to cold."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your resistance to cold extend."; }
    }

    public static class ResistFire extends Condition implements Serializable {
        public ResistFire(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " are resistant to fire."; }
        @Override String getEndMessage() { return actor.getNoun() + " are susceptible to fire."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " are more resistant to fire."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " are less resistant to fire."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your resistance to fire extend."; }
    }

    public static class ResistAcid extends Condition implements Serializable {
        public ResistAcid(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " are resistant to acid."; }
        @Override String getEndMessage() { return actor.getNoun() + " are susceptible to acid."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " are more resistant to acid."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " are less resistant to acid."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your resistance to acid extend."; }
    }

    public static class ResistPoison extends Condition implements Serializable {
        public ResistPoison(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " are resistant to poison."; }
        @Override String getEndMessage() { return actor.getNoun() + " are susceptible to poison."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " are more resistant to poison."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " are less resistant to poison."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your resistance to poison extend."; }
    }

    public static class ResistElectricity extends Condition implements Serializable {
        public ResistElectricity(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getNoun() + " are resistant to electricity."; }
        @Override String getEndMessage() { return actor.getNoun() + " are susceptible to electricity."; }
        @Override String getIncreaseMessage() { return actor.getNoun() + " are more resistant to electricity."; }
        @Override String getDecreaseMessage() { return actor.getNoun() + " are less resistant to electricity."; }
        @Override String getExtendMessage() { return actor.getNoun() + " feel your resistance to electricity extend."; }
    }
}
