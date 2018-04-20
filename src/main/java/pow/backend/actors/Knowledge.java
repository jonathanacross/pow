package pow.backend.actors;

import pow.backend.AttackData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Knowledge implements Serializable {

    public static class MonsterSummary implements Serializable {
        public final String id;
        public final String description;
        public final String name;
        public final int strength;
        public final int dexterity;
        public final int intelligence;
        public final int constitution;
        public final int health;
        public final int maxHealth;
        public final int mana;
        public final int maxMana;
        public final int level;
        public final AttackData primaryAttack;
        public final int defense;
        public final int speed;
        public final int experience;
        public final String image;

        public int numKilled;

        public MonsterSummary(Actor m) {
            this.id = m.id;
            this.description = m.description;
            this.name = m.name;
            this.strength = m.baseStats.strength;
            this.dexterity = m.baseStats.dexterity;
            this.intelligence = m.baseStats.intelligence;
            this.constitution = m.baseStats.constitution;
            this.health = m.health;
            this.maxHealth = m.getMaxHealth();
            this.mana = m.getMana();
            this.maxMana = m.getMaxMana();
            this.level = m.level;
            this.primaryAttack = m.getPrimaryAttack();
            this.defense = m.getDefense();
            this.speed = m.getSpeed();
            this.experience = m.experience;
            this.image = m.image;

            this.numKilled = 0;
        }

        public void incrementKillCount() { this.numKilled++; }
    }

    private final Map<String,MonsterSummary> monsterKnowledge;

    public Knowledge() {
        monsterKnowledge = new HashMap<>();
    }

    public void addMonster(Actor m) {
        if (monsterKnowledge.containsKey(m.id)) return;
        monsterKnowledge.put(m.id, new MonsterSummary(m));
    }

    public void incrementKillCount(Actor m) {
        if (m == null) return;   // skip events not involving some source

        addMonster(m);
        monsterKnowledge.get(m.id).incrementKillCount();
    }

    public List<MonsterSummary> getMonsterSummary() {
        List<MonsterSummary> summaryList = new ArrayList<>(monsterKnowledge.values());
        summaryList.sort((MonsterSummary m1, MonsterSummary m2) -> {
            if (m1.experience != m2.experience) { return m1.experience - m2.experience; }
            if (m1.level != m2.level) { return m1.level - m2.level; }
            else { return m1.name.compareTo(m2.name); }
        });
        return summaryList;
    }
}
