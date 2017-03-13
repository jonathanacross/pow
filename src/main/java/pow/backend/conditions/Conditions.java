package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Conditions {
    public static class Health extends Condition implements Serializable {

        public Health(Actor actor) {
            super(actor);
        }

        @Override
        String getStartMessage() {
            return actor.getPronoun() + " feel vigorous.";
        }

        @Override
        String getEndMessage() {
            return actor.getPronoun() + " return to normal.";
        }

        @Override
        protected List<GameEvent> startImpl(GameBackend backend) {
            actor.increaseHealth(getIntensity());
            return Collections.singletonList(GameEvent.DungeonUpdated());
        }

        @Override
        protected List<GameEvent> endImpl(GameBackend backend) {
            // force update of health to stay within limit
            actor.increaseHealth(getIntensity());
            return Collections.singletonList(GameEvent.DungeonUpdated());
        }
    }

    public static class Poison extends Condition implements Serializable {
        public Poison(Actor actor) {
            super(actor);
        }

        @Override
        String getStartMessage(){
            return(actor.getPronoun() + " are poisoned!");
        }

        @Override
        String getEndMessage() {
            return(actor.getPronoun() + " recover from the poison.");
        }

        @Override
        protected List<GameEvent> updateImpl(GameBackend backend) {
            return actor.takeDamage(backend, getIntensity());
        }
    }

    public static class Speed extends Condition implements Serializable {
        public Speed(Actor actor) {
            super(actor);
        }

        @Override
        String getStartMessage() { return actor.getPronoun() + " start moving faster!"; }

        @Override
        String getEndMessage() { return actor.getPronoun() + " return to normal speed."; }
    }

    public static class ToHit extends Condition implements Serializable {
        public ToHit(Actor actor) { super(actor); }

        @Override
        String getStartMessage() { return actor.getPronoun() + " can hit more accurately!"; }

        @Override
        String getEndMessage() { return actor.getPronoun() + " feel your accuracy return to normal."; }
    }

    public static class ToDam extends Condition implements Serializable {
        public ToDam(Actor actor) { super(actor); }

        @Override
        String getStartMessage() { return actor.getPronoun() + " can hit harder!"; }

        @Override
        String getEndMessage() { return actor.getPronoun() + " feel your damage return to normal."; }
    }

    public static class Defense extends Condition implements Serializable {
        public Defense(Actor actor) { super(actor); }

        @Override
        String getStartMessage() { return actor.getPronoun() + " are harder to hit!"; }

        @Override
        String getEndMessage() { return actor.getPronoun() + " defense returns to normal."; }
    }
}
