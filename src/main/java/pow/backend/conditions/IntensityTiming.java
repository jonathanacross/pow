package pow.backend.conditions;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

// helper class to keep track of changing intensities over time
public class IntensityTiming implements Serializable {
    private static class IntensityDuration implements Serializable {
        public final int intensity;
        public int turnsRemaining;

        public IntensityDuration(int intensity, int turnsRemaining) {
            this.intensity = intensity;
            this.turnsRemaining = turnsRemaining;
        }
    }

    private final List<IntensityDuration> intensities;

    public IntensityTiming() {
        this.intensities = new LinkedList<>();
    }

    public int getIntensity() {
        int maxIntensity = 0;
        for (IntensityDuration di : intensities) {
            maxIntensity = Math.max(maxIntensity, di.intensity);
        }
        return maxIntensity;
    }

    public int getTurnsRemaining() {
        int maxTurns = 0;
        for (IntensityDuration di : intensities) {
            maxTurns = Math.max(maxTurns, di.turnsRemaining);
        }
        return maxTurns;
    }

    public boolean active() {
        for (IntensityDuration di : intensities) {
            if (di.turnsRemaining > 0) {
                return true;
            }
        }
        return false;
    }

    public void decrementTurnsRemaining() {
        // decrement turns remaining for all entries
        for (IntensityDuration di : intensities) {
            di.turnsRemaining--;
        }

        // clean up: remove any entries with time <= 0
        intensities.removeIf(intensityDuration -> intensityDuration.turnsRemaining <= 0);
    }

    public void add(int intensity, int duration) {
        intensities.add(new IntensityDuration(intensity, duration));
    }
}
