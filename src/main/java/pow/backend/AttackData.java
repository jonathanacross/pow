package pow.backend;

import pow.util.DieRoll;

import java.io.Serializable;

public class AttackData implements Serializable {
    public DieRoll dieRoll;
    public int plusToHit;
    public int plusToDam;

    public AttackData(DieRoll dieRoll, int plusToHit, int plusToDam) {
        this.dieRoll = dieRoll;
        this.plusToHit = plusToHit;
        this.plusToDam = plusToDam;
    }

    // TODO:common code with DungeonItem
    private static String formatBonus(int x) {
        if (x < 0) { return "-" + (-x); }
        else { return "+" + x; }
    }

    @Override
    public String toString() {
        return this.dieRoll.toString() + " (" + formatBonus(plusToHit) + ", " + formatBonus(plusToDam) + ")";
    }
}