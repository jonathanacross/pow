package pow.backend.conditions;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

public abstract class Condition {
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

    protected void startImpl(GameBackend backend) {}  // override these three for custom behavior
    protected void endImpl(GameBackend backend) {}
    protected void updateImpl(GameBackend backend) {}

    public void start(GameBackend backend, int turnsRemaining, int intensity) {
        if (this.turnsRemaining > 0) {
            // Cancel any previous effect.  This is important so that effects that modify
            // an actor's state (e.g., adding health) can restore back to normal
            // in case the intensity changes.
            endImpl(backend);
        }
        this.turnsRemaining = turnsRemaining;
        this.intensity = intensity;
        startImpl(backend);
        backend.logMessage(getStartMessage());
    }

    public void end(GameBackend backend) {
        endImpl(backend);
        this.turnsRemaining = 0;
        this.intensity = 0;
        backend.logMessage(getEndMessage());
    }

    public void update(GameBackend backend) {
        if (turnsRemaining > 0) {
            updateImpl(backend);
            turnsRemaining--;

            if (turnsRemaining == 0) {
                end(backend);
            }
        }
    }
}
