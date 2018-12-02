package pow.backend.actors;

import java.io.Serializable;

public class Abilities implements Serializable {
    // actor is especially good at archery
    public final boolean archeryBonus;

    // actor may do extra poison damage
    public final boolean poisonDamage;

    // actor may do extra stun damage
    public final boolean stunDamage;

    public Abilities(boolean archeryBonus, boolean poisonDamage, boolean stunDamage) {
        this.archeryBonus = archeryBonus;
        this.poisonDamage = poisonDamage;
        this.stunDamage = stunDamage;
    }

    public Abilities() {
        this(false, false, false);
    }
}
