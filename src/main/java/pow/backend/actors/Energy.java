package pow.backend.actors;

/// Energy is used to control the rate that actors move relative to other
/// actors. Each game turn, every actor will accumulate energy based on their
/// speed. When it reaches a threshold, that actor can take a turn.
public class Energy {
    // TODO: fix name constants
    static final int minSpeed    = 0;
    static final int normalSpeed = 6;
    static final int maxSpeed    = 12;

    static final int actionCost = 240;

    // How much energy is gained each game turn for each speed.
    static final int[] gains = {
            15,     // 1/4 normal speed
            20,     // 1/3 normal speed
            25,
            30,     // 1/2 normal speed
            40,
            50,
            60,     // normal speed
            80,
            100,
            120,    // 2x normal speed
            150,
            180,    // 3x normal speed
            240     // 4x normal speed
            };

//    static num ticksAtSpeed(int speed) => actionCost / gains[normalSpeed + speed];

    public int energy = 0; // TODO: make private?

    boolean canTakeTurn() {
        return energy >= actionCost;
    }

    /// Advances one game turn and gains an appropriate amount of energy. Returns
    /// `true` if there is enough energy to take a turn.
    boolean gain(int speed) {
        energy += gains[speed];
        return canTakeTurn();
    }

    /// Spends a turn's worth of energy.
    void spend() {
        assert(energy >= actionCost);
        energy -= actionCost;
    }
}
