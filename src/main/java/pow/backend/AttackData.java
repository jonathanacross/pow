package pow.backend;

import pow.util.DieRoll;
import pow.util.TextUtils;

import java.io.Serializable;

public class AttackData implements Serializable {
    public final DieRoll dieRoll;
    public final int plusToHit;
    public final int plusToDam;

    public AttackData(DieRoll dieRoll, int plusToHit, int plusToDam) {
        this.dieRoll = dieRoll;
        this.plusToHit = plusToHit;
        this.plusToDam = plusToDam;
    }

    @Override
    public String toString() {
        return this.dieRoll.toString() + " (" + TextUtils.formatBonus(plusToHit) + ", " + TextUtils.formatBonus(plusToDam) + ")";
    }

    public double getAverageDamage() {
        return plusToDam + 0.5 * dieRoll.roll * (dieRoll.die + 1);
    }

    public double getVariance() { return dieRoll.roll * (dieRoll.die * dieRoll.die - 1.0) / 12.0; }
}
