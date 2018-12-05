package pow.backend.conditions;

import pow.backend.actors.Actor;

import java.util.function.Function;

public enum ConditionTypes {
    HEALTH(Conditions.Health::new),
    POISON(Conditions.Poison::new),
    STUN(Conditions.Stun::new),
    CONFUSE(Conditions.Confuse::new),
    SPEED(Conditions.Speed::new),
    TO_HIT(Conditions.ToHit::new),
    TO_DAM(Conditions.ToDam::new),
    DEFENSE(Conditions.Defense::new),
    RESIST_COLD(Conditions.ResistCold::new),
    RESIST_FIRE(Conditions.ResistFire::new),
    RESIST_ACID(Conditions.ResistAcid::new),
    RESIST_POIS(Conditions.ResistPoison::new),
    RESIST_ELEC(Conditions.ResistElectricity::new),
    RESIST_DAM(Conditions.ResistDamage::new);

    private final Function<Actor, Condition> instanceGenerator;

    ConditionTypes(Function<Actor, Condition> instanceGenerator) {
       this.instanceGenerator = instanceGenerator;
    }

    public Condition getInstance(Actor actor) {
        return instanceGenerator.apply(actor);
    }
}
