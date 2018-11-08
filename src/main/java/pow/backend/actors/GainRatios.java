package pow.backend.actors;

import java.io.Serializable;

public class GainRatios implements Serializable {
    public final String id;
    public final double strRatio;
    public final double dexRatio;
    public final double intRatio;
    public final double conRatio;
    public final double speedRatio;

    public GainRatios(String id, double strRatio, double dexRatio, double intRatio, double conRatio, double speedRatio) {
        this.id = id;
        this.strRatio = strRatio;
        this.dexRatio = dexRatio;
        this.intRatio = intRatio;
        this.conRatio = conRatio;
        this.speedRatio = speedRatio;
    }
}
