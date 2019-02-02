package pow.backend;

import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.actors.StatComputations;
import pow.backend.conditions.ConditionTypes;
import pow.util.DieRoll;
import pow.util.Point;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;


public class SpellParams implements Serializable {

    // The type of action or spell area for this spell.
    public enum SpellType implements Serializable {
        ARROW,
        BALL,
        BOOST_ARMOR,
        BOOST_ATTACK,
        BREATH,
        CHAIN,
        CIRCLE_CUT,
        HEAL,
        GROUP_HEAL,
        BOLT,
        PHASE,
        CALL_PET,
        QUAKE,
        RESIST_ELEMENTS,
        SPEED
    }

    public static boolean isAttackSpell(SpellParams params) {
        SpellType spellType = params.spellType;
        return spellType == SpellType.ARROW ||
                spellType == SpellType.BALL ||
                spellType == SpellType.BREATH ||
                spellType == SpellType.CHAIN ||
                spellType == SpellType.CIRCLE_CUT ||
                spellType == SpellType.BOLT ||
                spellType == SpellType.QUAKE;
    }

    // For area spells, what element/effect is done?
    public enum Element implements Serializable {
        NONE,
        ACID,
        CONFUSE,
        DAMAGE,
        FIRE,
        ICE,
        LIGHTNING,
        POISON,
        SLEEP,
        STUN
    }

    // What stat is used to control how powerful the spell should be?
    public enum PowerStat implements Serializable {
        NONE,
        STRENGTH,
        DEXTERITY,
        INTELLIGENCE,
        CONSTITUTION,
        ATTACK
    }

    public final String id;
    public final String name;
    private final String description;
    private final String castMessage; // message logged when casted
    public final int minLevel; // min level for a character to cast this
    public final int requiredMana;
    public final SpellType spellType;
    public final Element element;
    private final PowerStat powerStat;
    public final int size;  // related to size of area affected by this spell (for area spells)
    public final int duration; // how long the effect lacks (for poison, buffing spells, etc)
    private final double primaryAmtBase;  // primary amount will be a die roll
    private final double primaryAmtDelta;
    private final double secondaryAmtBase;  // secondary amount is fixed
    private final double secondaryAmtDelta;
    public final boolean requiresTarget;

    public SpellParams(String id,
                       String name,
                       String description,
                       String castMessage,
                       int minLevel,
                       int requiredMana,
                       SpellType spellType,
                       Element element,
                       PowerStat powerStat,
                       int size,
                       int duration,
                       double primaryAmtBase,
                       double primaryAmtDelta,
                       double secondaryAmtBase,
                       double secondaryAmtDelta) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.castMessage = castMessage;
        this.minLevel = minLevel;
        this.requiredMana = requiredMana;
        this.spellType = spellType;
        this.element = element;
        this.powerStat = powerStat;
        this.size = size;
        this.duration = duration;
        this.primaryAmtBase = primaryAmtBase;
        this.primaryAmtDelta = primaryAmtDelta;
        this.secondaryAmtBase = secondaryAmtBase;
        this.secondaryAmtDelta = secondaryAmtDelta;
        this.requiresTarget = (
                spellType == SpellType.ARROW ||
                spellType == SpellType.BALL ||
                spellType == SpellType.BOLT ||
                spellType == SpellType.BREATH);
    }

    private double getAmount(Actor actor, double base, double delta, PowerStat powerStat) {
        double multiplier = 0.0;
        switch (powerStat) {
            case NONE:
                multiplier = 0;
                break;
            case ATTACK:
                multiplier = actor.getPrimaryAttack().getAverageDamage();
                break;
            case STRENGTH:
                multiplier = actor.baseStats.strength;
                break;
            case DEXTERITY:
                multiplier = actor.baseStats.dexterity;
                break;
            case INTELLIGENCE:
                multiplier = actor.baseStats.intelligence;
                break;
            case CONSTITUTION:
                multiplier = actor.baseStats.constitution;
                break;
        }
        return base + delta * multiplier;
    }

    public DieRoll getPrimaryAmount(Actor actor) {
        return StatComputations.findClosestDieRoll(getAmount(actor, primaryAmtBase, primaryAmtDelta, powerStat));
    }

    public int getSecondaryAmount(Actor actor) {
        return (int) Math.round(getAmount(actor, secondaryAmtBase, secondaryAmtDelta, powerStat));
    }

    public String getCastMessage(Actor actor) {
        return actor.getNoun() + " " + castMessage;
    }

    public String getDescription(Actor actor) {
        return description
                .replace("{1}", getPrimaryAmount(actor).toString())
                .replace("{2}", Integer.toString(getSecondaryAmount(actor)))
                .replace("{s}", Integer.toString(size))
                .replace("{t}", Integer.toString(duration));
    }

    public static Action buildAction(SpellParams spellParams, Actor actor, Point target) {
        int secondaryAmount = spellParams.getSecondaryAmount(actor);
        switch (spellParams.spellType) {
            case ARROW:
                return new SpellAction(new ArrowSpell(actor, target, spellParams), spellParams);
            case PHASE:
                return new SpellAction(new Phase(actor, spellParams.size), spellParams);
            case CALL_PET:
                return new SpellAction(new CallPet(actor), spellParams);
            case HEAL:
                return new SpellAction(new Heal(actor, secondaryAmount), spellParams);
            case GROUP_HEAL:
                return new SpellAction(new GroupHeal(actor, secondaryAmount), spellParams);
            case BALL:
                return new SpellAction(new BallSpell(actor, target, spellParams), spellParams);
            case BOLT:
                return new SpellAction(new BoltSpell(actor, target, spellParams), spellParams);
            case CHAIN:
                return new SpellAction(new ChainSpell(actor, spellParams), spellParams);
            case BREATH:
                return new SpellAction(new BreathSpell(actor, target, spellParams), spellParams);
            case QUAKE:
                return new SpellAction(new QuakeSpell(actor, spellParams), spellParams);
            case CIRCLE_CUT:
                return new SpellAction(new CircleCut(actor, spellParams), spellParams);
            case BOOST_ARMOR:
                return new SpellAction(new StartCondition(actor,
                        Collections.singletonList(ConditionTypes.DEFENSE), spellParams.duration, secondaryAmount), spellParams);
            case BOOST_ATTACK:
                return new SpellAction(new StartCondition(actor,
                        Collections.singletonList(ConditionTypes.TO_HIT), spellParams.duration, secondaryAmount), spellParams);
            case RESIST_ELEMENTS:
                return new SpellAction(new StartCondition(actor,
                        Arrays.asList(ConditionTypes.RESIST_COLD, ConditionTypes.RESIST_FIRE,
                                ConditionTypes.RESIST_ACID, ConditionTypes.RESIST_POIS,
                                ConditionTypes.RESIST_ELEC, ConditionTypes.RESIST_DAM),
                        spellParams.duration, secondaryAmount), spellParams);
            case SPEED:
                return new SpellAction(new StartCondition(actor,
                        Collections.singletonList(ConditionTypes.SPEED), spellParams.duration, secondaryAmount), spellParams);
        }
        throw new RuntimeException("tried to create unknown spell from " + spellParams.name);
    }
}
