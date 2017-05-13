package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Condition implements Serializable {
    private IntensityTiming intensityTiming;
    protected final Actor actor; // actor associated with this condition

    abstract String getStartMessage();
    abstract String getIncreaseMessage();
    abstract String getDecreaseMessage();
    abstract String getExtendMessage();
    abstract String getEndMessage();
    public int getIntensity() { return intensityTiming.getIntensity(); }

    public Condition(Actor actor) {
        this.actor = actor;
        this.intensityTiming = new IntensityTiming();
    }

    // called each turn
    protected List<GameEvent> updateImpl(GameBackend backend) { return new ArrayList<>(); }

    // called each time the value changes (or a new condition was stacked)
    protected List<GameEvent> changeImpl(GameBackend backend, int delta) { return new ArrayList<>(); }

    public List<GameEvent> start(GameBackend backend, int turnsRemaining, int intensity) {
        int currIntensity = this.intensityTiming.getIntensity();
        int currTurnsRemaining = this.intensityTiming.getTurnsRemaining();
        List<GameEvent> events = new ArrayList<>();

        // update user with change in status
        if (!this.intensityTiming.active()) {
            backend.logMessage(getStartMessage());
        } else {
            if (intensity > currIntensity) {
                backend.logMessage(getIncreaseMessage());
            } else if (turnsRemaining > currTurnsRemaining) {
                backend.logMessage(getExtendMessage());
            } else {
                backend.logMessage("but it has no effect");
            }
        }

        this.intensityTiming.add(intensity, turnsRemaining);
        if (intensity - currIntensity != 0) {
            events.addAll(changeImpl(backend, intensity - currIntensity));
        }
        return events;
    }

    public List<GameEvent> update(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();

        // skip processing if nothing active
        if (!intensityTiming.active()) return events;

        // process the condition, if necessary.
        int currIntensity = intensityTiming.getIntensity();
        events.addAll(updateImpl(backend));
        intensityTiming.decrementTurnsRemaining();

        // if there's a change, then log for the user
        int newIntensity = intensityTiming.getIntensity();
        if (!this.intensityTiming.active()) {
            backend.logMessage(getEndMessage());
        } else {
            if (currIntensity < newIntensity) {
                backend.logMessage(getIncreaseMessage());
            } else if (currIntensity > newIntensity) {
                backend.logMessage(getDecreaseMessage());
            }
        }

        if (newIntensity != currIntensity) {
            events.addAll(changeImpl(backend, newIntensity - currIntensity));
        }

        return events;
    }
}
