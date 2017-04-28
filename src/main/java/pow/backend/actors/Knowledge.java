package pow.backend.actors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Knowledge implements Serializable {

    public static class MonsterSummary implements Serializable {
        public final String id;
        public final String name;
        public final int level;
        public final String image;
        public final int experience;
        public int numKilled;

        public MonsterSummary(String id, String name, int level, String image, int experience) {
            this.id = id;
            // TODO: remove everything except id and numKilled?
            this.name = name;
            this.level = level;
            this.image = image;
            this.experience = experience;
            this.numKilled = 0;
        }

        public void incrementKillCount() { this.numKilled++; }
    }

    private Map<String,MonsterSummary> monsterKnowledge;

    public Knowledge() {
        monsterKnowledge = new HashMap<>();
    }

    public void addMonster(Actor m) {
        if (monsterKnowledge.containsKey(m.id)) return;
        monsterKnowledge.put(m.id, new MonsterSummary(m.id, m.name, m.level, m.image, m.experience));
    }

    public void incrementKillCount(Actor m) {
        if (m == null) return;   // skip events not involving some source

        addMonster(m);
        monsterKnowledge.get(m.id).incrementKillCount();
    }

    public List<MonsterSummary> getMonsterSummary() {
        List<MonsterSummary> summaryList = new ArrayList<>();
        summaryList.addAll(monsterKnowledge.values());
        summaryList.sort((MonsterSummary m1, MonsterSummary m2) -> {
            if (m1.experience != m2.experience) { return m1.experience - m2.experience; }
            if (m1.level != m2.level) { return m1.level - m2.level; }
            else { return m1.name.compareTo(m2.name); }
        });
        return summaryList;
    }
}
