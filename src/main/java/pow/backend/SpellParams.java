package pow.backend;

import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.util.DieRoll;
import pow.util.Point;

import java.io.Serializable;

public class SpellParams implements Serializable {

    // The type of action or spell area for this spell.
    public enum SpellType implements Serializable {
        ARROW,
        BALL,
        BOOST_ARMOR,
        BREATH,
        CHAIN,
        CIRCLE_CUT,
        HEAL,
        BOLT,
        PHASE,
        QUAKE,
        RESIST_ELEMENTS,
        SPEED
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
    public final int minLevel; // min level for a character to cast this
    public final int requiredMana;
    public final SpellType spellType;
    public final Element element;
    private final PowerStat powerStat;
    public final int size;  // related to size of area affected by this spell (for area spells)
    private final int amtBase;
    private final int amtDelta;  // total value for this spell will be amtBase + amtDelta*level
    public final boolean requiresTarget;

    public SpellParams(String id,
                       String name,
                       String description,
                       int minLevel,
                       int requiredMana,
                       SpellType spellType,
                       Element element,
                       PowerStat powerStat,
                       int size,
                       int amtBase,
                       int amtDelta) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.minLevel = minLevel;
        this.requiredMana = requiredMana;
        this.spellType = spellType;
        this.element = element;
        this.powerStat = powerStat;
        this.size = size;
        this.amtBase = amtBase;
        this.amtDelta = amtDelta;
        this.requiresTarget = (
                spellType == SpellType.ARROW ||
                spellType == SpellType.BALL ||
                spellType == SpellType.BOLT ||
                spellType == SpellType.BREATH);
    }

    public int getAmount(Actor actor) {
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
        return (int) Math.round(amtBase + amtDelta * multiplier);
    }

    public String getDescription(Actor actor) {
        return description.replace("{}", Integer.toString(getAmount(actor)));
    }

    public static Action buildAction(SpellParams spellParams, Actor actor, Point target) {
        int amount = spellParams.getAmount(actor);
        AttackData attackData = new AttackData(new DieRoll(0,0), amount, amount);
        boolean hitMagically = spellParams.element != Element.NONE;
        switch (spellParams.spellType) {
            case ARROW: return new SpellAction(new Arrow(actor, target, attackData, false, hitMagically), spellParams);
            case PHASE: return new SpellAction(new Phase(actor, amount), spellParams);
            case HEAL: return new SpellAction(new Heal(actor, amount), spellParams);
            case BALL: return new SpellAction(new BallSpell(actor, target, spellParams), spellParams);
            case BOLT: return new SpellAction(new BoltSpell(actor, target, spellParams), spellParams);
            case CHAIN: return new SpellAction(new ChainSpell(actor, spellParams), spellParams);
            case BREATH: return new SpellAction(new BreathSpell(actor, target, spellParams), spellParams);
            case QUAKE: return new SpellAction(new QuakeSpell(actor, spellParams), spellParams);
            case CIRCLE_CUT: return new SpellAction(new CircleCut(actor, spellParams), spellParams);
            default: break;
        }
        throw new RuntimeException("tried to create unknown spell from " + spellParams.name);
    }
}
