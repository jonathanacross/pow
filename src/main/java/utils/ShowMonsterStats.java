package utils;

import pow.backend.actors.Monster;
import pow.backend.dungeon.gen.MonsterGenerator;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Utility for displaying stats for all monsters, helpful for tuning.
public class ShowMonsterStats {

    public static void main(String[] args) {

        // get all the monsters, sorted by level
        Random rng = new Random(123);
        Set<String> allMonsters = MonsterGenerator.getMonsterIds();
        List<Monster> monsters = new ArrayList<>();
        for (String id : allMonsters) {
            Monster m = MonsterGenerator.genMonster(id, rng, new Point(-1, -1));
            monsters.add(m);
        }

        monsters.sort(
            (Monster a, Monster b) -> {
                if (a.level != b.level) return a.level - b.level;
                if (a.experience != b.experience) return a.experience - b.experience;
                return a.name.compareTo(b.name);
            }
        );

        // output stats
        System.out.println(
                "level" + "\t" +
                "name" + "\t" +
                "strength" + "\t" +
                "dexterity" + "\t" +
                "intelligence" + "\t" +
                "constitution" + "\t" +
                "meleeToDam" + "\t" +
                "defense" + "\t" +
                "maxHealth" + "\t" +
                "maxMana" + "\t" +
                "speed" + "\t" +
                "experience");
        for (Monster m : monsters) {
            System.out.println(
                    m.level + "\t" +
                    m.name + "\t" +
                    m.baseStats.strength + "\t" +
                    m.baseStats.dexterity + "\t" +
                    m.baseStats.intelligence + "\t" +
                    m.baseStats.constitution + "\t" +
                    m.baseStats.meleeToDam + "\t" +
                    m.baseStats.defense + "\t" +
                    m.baseStats.maxHealth + "\t" +
                    m.baseStats.maxMana + "\t" +
                    m.baseStats.speed + "\t" +
                    m.experience);
        }
    }
}
