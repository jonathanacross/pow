package pow.backend.actors;

import java.io.Serializable;

public class Abilities implements Serializable {
    // actor is especially good at archery
    public final boolean archeryBonus;

    // actor does extra long-term damage
    public final boolean continuedDamage;

    public Abilities(boolean archeryBonus, boolean continuedDamage) {
        this.archeryBonus = archeryBonus;
        this.continuedDamage = continuedDamage;
    }

    public Abilities() {
        this(false, false);
    }
}
