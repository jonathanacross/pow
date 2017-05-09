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
            return actor.getPronoun() + " feel vigorous.";
        }
        @Override String getEndMessage() {
            return actor.getPronoun() + " return to normal.";
        }
        @Override String getIncreaseMessage() { return actor.getPronoun() + " feel more vigorous."; }
        @Override String getDecreaseMessage() { return actor.getPronoun() + " feel somewhat less vigorous."; }
        @Override String getExtendMessage() { return actor.getPronoun() + " feel the vigor extend."; }

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

        @Override String getStartMessage() { return(actor.getPronoun() + " are poisoned!"); }
        @Override String getEndMessage() { return(actor.getPronoun() + " recover from the poison."); }
        @Override String getIncreaseMessage() { return actor.getPronoun() + " feel more poisoned."; }
        @Override String getDecreaseMessage() { return actor.getPronoun() + " feel less poisoned."; }
        @Override String getExtendMessage() { return actor.getPronoun() + " feels the poison lasting longer."; }

        @Override
        protected List<GameEvent> updateImpl(GameBackend backend) {
            return actor.takeDamage(backend, getIntensity());
        }
    }

    public static class Speed extends Condition implements Serializable {
        public Speed(Actor actor) {
            super(actor);
        }

        @Override String getStartMessage() { return actor.getPronoun() + " start moving faster!"; }
        @Override String getEndMessage() { return actor.getPronoun() + " return to normal speed."; }
        @Override String getIncreaseMessage() { return actor.getPronoun() + " start moving even faster."; }
        @Override String getDecreaseMessage() { return actor.getPronoun() + " slow down some."; }
        @Override String getExtendMessage() { return actor.getPronoun() + " feels the haste lasting longer."; }
    }

    public static class ToHit extends Condition implements Serializable {
        public ToHit(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getPronoun() + " can hit more accurately!"; }
        @Override String getEndMessage() { return actor.getPronoun() + " feel your accuracy return to normal."; }
        @Override String getIncreaseMessage() { return actor.getPronoun() + " can hit even more accurately!"; }
        @Override String getDecreaseMessage() { return actor.getPronoun() + " can hit somewhat less accurately."; }
        @Override String getExtendMessage() { return actor.getPronoun() + " feel your accuracy extend."; }
    }

    public static class ToDam extends Condition implements Serializable {
        public ToDam(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getPronoun() + " can hit harder!"; }
        @Override String getEndMessage() { return actor.getPronoun() + " feel your damage return to normal."; }
        @Override String getIncreaseMessage() { return actor.getPronoun() + " can hit even harder!"; }
        @Override String getDecreaseMessage() { return actor.getPronoun() + " can hit somewhat less hard."; }
        @Override String getExtendMessage() { return actor.getPronoun() + " feel your damage extend."; }
    }

    public static class Defense extends Condition implements Serializable {
        public Defense(Actor actor) { super(actor); }

        @Override String getStartMessage() { return actor.getPronoun() + " are harder to hit!"; }
        @Override String getEndMessage() { return actor.getPronoun() + " defense returns to normal."; }
        @Override String getIncreaseMessage() { return actor.getPronoun() + " even harder to hit!"; }
        @Override String getDecreaseMessage() { return actor.getPronoun() + " defense decreases somewhat."; }
        @Override String getExtendMessage() { return actor.getPronoun() + " feel your defense extend."; }
    }
}
