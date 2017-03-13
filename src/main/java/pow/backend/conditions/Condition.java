package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Condition implements Serializable {
    private int turnsRemaining;
    private int intensity;
    protected Actor actor; // actor associated with this condition

    abstract String getStartMessage();
    abstract String getEndMessage();
    public int getIntensity() { return intensity; }

    public Condition(Actor actor) {
        this.actor = actor;
        this.intensity = 0;
        this.turnsRemaining = 0;
    }

    protected List<GameEvent> startImpl(GameBackend backend) { return new ArrayList<>(); }  // override these three for custom behavior
    protected List<GameEvent> endImpl(GameBackend backend) { return new ArrayList<>(); }
    protected List<GameEvent> updateImpl(GameBackend backend) { return new ArrayList<>(); }

    public List<GameEvent> start(GameBackend backend, int turnsRemaining, int intensity) {
        List<GameEvent> events = new ArrayList<>();
        if (this.turnsRemaining > 0) {
            // Cancel any previous effect.  This is important so that effects that modify
            // an actor's state (e.g., adding health) can restore back to normal
            // in case the intensity changes.
            events.addAll(endImpl(backend));
        }
        this.turnsRemaining = turnsRemaining;
        this.intensity = intensity;
        events.addAll(startImpl(backend));
        backend.logMessage(getStartMessage());
        return events;
    }

    public List<GameEvent> end(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        this.turnsRemaining = 0;
        this.intensity = 0;
        events.addAll(endImpl(backend));
        backend.logMessage(getEndMessage());
        return events;
    }

    public List<GameEvent> update(GameBackend backend) {
        List<GameEvent> events = new ArrayList<>();
        if (turnsRemaining > 0) {
            events.addAll(updateImpl(backend));
            turnsRemaining--;

            if (turnsRemaining == 0) {
                events.addAll(end(backend));
            }
        }
        return events;
    }
}
