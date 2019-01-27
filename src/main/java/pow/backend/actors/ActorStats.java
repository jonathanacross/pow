package pow.backend.actors;

import pow.backend.dungeon.DungeonItem;
import pow.util.DieRoll;

import java.io.Serializable;
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
    // magical resistances
    public final int resFire;
    public final int resCold;
    public final int resAcid;
    public final int resElec;
    public final int resPois;
    public final int resDam;

    public ActorStats(int strength, int dexterity, int intelligence, int constitution, int speed, List<DungeonItem> equipment, boolean archeryBonus, int extraResistBonus) {
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
        int resFireBonus = extraResistBonus;
        int resColdBonus = extraResistBonus;
        int resAcidBonus = extraResistBonus;
        int resElecBonus = extraResistBonus;
        int resPoisBonus = extraResistBonus;
        int resDamBonus = extraResistBonus;

        for (DungeonItem item : equipment) {
            strBonus += item.bonuses[DungeonItem.STR_IDX];
            dexBonus += item.bonuses[DungeonItem.DEX_IDX];
            intBonus += item.bonuses[DungeonItem.INT_IDX];
            conBonus += item.bonuses[DungeonItem.CON_IDX];
            spdBonus += item.bonuses[DungeonItem.SPEED_IDX];
            defBonus += item.bonuses[DungeonItem.DEF_IDX];
            resFireBonus += item.bonuses[DungeonItem.RES_FIRE_IDX];
            resColdBonus += item.bonuses[DungeonItem.RES_COLD_IDX];
            resAcidBonus += item.bonuses[DungeonItem.RES_ACID_IDX];
            resElecBonus += item.bonuses[DungeonItem.RES_ELEC_IDX];
            resPoisBonus += item.bonuses[DungeonItem.RES_POIS_IDX];
            resDamBonus += item.bonuses[DungeonItem.RES_DAM_IDX];

            switch (item.slot) {
                case WEAPON:
                    meleeToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                    meleeToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                    break;
                case BOW:
                    rangedToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                    rangedToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                    break;
                default:
                    // toHit, toDam applies to both ranged and melee for magical items
                    meleeToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                    meleeToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                    rangedToHitBonus += item.bonuses[DungeonItem.TO_HIT_IDX];
                    rangedToDamBonus += item.bonuses[DungeonItem.TO_DAM_IDX];
                    break;
            }
        }

        // update base stats
        this.strength = strength + strBonus;
        this.dexterity = dexterity + dexBonus;
        this.intelligence = intelligence + intBonus;
        this.constitution = constitution + conBonus;
        this.speed = speed + spdBonus;

        // compute derived stats
        this.maxHealth = StatComputations.constitutionToHealth(this.constitution);
        this.maxMana = StatComputations.intelligenceToMana(this.intelligence);

        int agility = StatComputations.dexterityToDefenseAndAttack(this.dexterity);
        int baseDamage = StatComputations.strengthToDamage(this.strength);
        this.defense = agility + defBonus;
        this.meleeToHit = agility + meleeToHitBonus;
        this.meleeToDam = meleeToDamBonus;
        this.rangedToHit = agility + rangedToHitBonus;
        this.rangedToDam = rangedToDamBonus;
        this.meleeDieRoll = StatComputations.findClosestDieRoll(baseDamage);
        double rangedMultiplier = archeryBonus ? 1.25 : 0.75;
        this.rangedDieRoll = StatComputations.findClosestDieRoll(rangedMultiplier * baseDamage);

        this.resFire = resFireBonus;
        this.resCold = resColdBonus;
        this.resAcid = resAcidBonus;
        this.resElec = resElecBonus;
        this.resPois = resPoisBonus;
        this.resDam = resDamBonus;
    }
}
