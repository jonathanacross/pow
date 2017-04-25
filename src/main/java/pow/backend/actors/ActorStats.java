package pow.backend.actors;

import pow.backend.dungeon.DungeonItem;
import pow.util.DieRoll;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

// Holds stats for actors; these control how actors interact with each
// other and the world.
public class ActorStats implements Serializable {
    public final int strength;
    public final int dexterity;
    public final int intelligence;
    public final int constitution;
    public final int speed;

    public final int defense;
    public final DieRoll meleeDieRoll;
    public final DieRoll rangedDieRoll;
    public final int meleeToHit;
    public final int rangedToHit;
    public final int meleeToDam;
    public final int rangedToDam;
    public final int maxHealth;
    public final int maxMana;

    // temporary constructor for monsters.. will remove once they use the 4 main stats
    public ActorStats(Actor.Params params) {
        this.strength = 0;
        this.dexterity = 0;
        this.intelligence = 0;
        this.constitution = 0;
        this.speed = params.speed;
        this.defense = params.defense;
        this.meleeDieRoll = params.attack.dieRoll;
        this.meleeToHit = params.attack.plusToHit;
        this.meleeToDam = params.attack.plusToDam;
        this.rangedDieRoll = params.attack.dieRoll;
        this.rangedToHit = params.attack.plusToHit;
        this.rangedToDam = params.attack.plusToDam;
        this.maxHealth = params.maxHealth;
        this.maxMana = params.maxMana;
    }

    public ActorStats(int strength, int dexterity, int intelligence, int constitution, int speed) {
        this(strength, dexterity, intelligence, constitution, speed, Collections.EMPTY_LIST);
    }

    public ActorStats(int strength, int dexterity, int intelligence, int constitution, int speed, List<DungeonItem> equipment) {
        // compute equipment bonuses
        int strBonus = 0;
        int dexBonus = 0;
        int intBonus = 0;
        int conBonus = 0;
        int spdBonus = 0;
        int defBonus = 0;
        int meleeToHitBonus = 0;
        int meleeToDamBonus = 0;
        int rangedToHitBonus = 0;
        int rangedToDamBonus = 0;
        for (DungeonItem item : equipment) {
            strBonus += item.bonuses[DungeonItem.STR_IDX];
            dexBonus += item.bonuses[DungeonItem.DEX_IDX];
            intBonus += item.bonuses[DungeonItem.INT_IDX];
            conBonus += item.bonuses[DungeonItem.CON_IDX];
            spdBonus += item.bonuses[DungeonItem.SPEED_IDX];
            defBonus += item.bonuses[DungeonItem.DEF_IDX];

            if (item.slot == DungeonItem.Slot.WEAPON) {
                meleeToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                meleeToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
            } else if (item.slot == DungeonItem.Slot.BOW) {
                rangedToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                rangedToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
            } else {
                // toHit, toDam applies to both ranged and melee for magical items
                meleeToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                meleeToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                rangedToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                rangedToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
            }
        }

        // update base stats
        this.strength = strength + strBonus;
        this.dexterity = dexterity + dexBonus;
        this.intelligence = intelligence + intBonus;
        this.constitution = constitution + conBonus;
        this.speed = speed + spdBonus;

        // compute derived stats
        this.maxHealth = StatConversions.CON_TO_HEALTH.getPoints(this.constitution);
        this.maxMana = StatConversions.INT_TO_MANA.getPoints(this.intelligence);

        int agility = StatConversions.DEX_TO_DEFENSE_AND_ATTACK.getPoints(this.dexterity);
        int baseDamage = StatConversions.STR_TO_DAMAGE.getPoints(this.strength);
        this.defense = agility + defBonus;
        this.meleeToHit = agility + meleeToHitBonus;
        this.meleeToDam = agility + meleeToDamBonus;
        this.rangedToHit = (int) Math.round(0.75 * agility) + rangedToHitBonus;
        this.rangedToDam = (int) Math.round(0.75 * agility) + rangedToDamBonus;
        this.meleeDieRoll = StatConversions.findClosestDieRoll(baseDamage);
        this.rangedDieRoll = StatConversions.findClosestDieRoll(0.75 * baseDamage);
    }
}
