package pow.backend.dungeon;

import pow.backend.dungeon.gen.MonsterGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MonsterIdGroup implements Serializable {

    // Monsters possible to generate.  If empty, then no monsters will be made.
    private final List<String> monsterIds;

    // Is it possible to generate a boss?  This will be false in two cases:
    // 1. There is no boss
    // 2. The boss was already generated at some point.
    public boolean canGenBoss;

    // Boss (if any) to create for this level.  Null if no boss.
    public final String bossId;

    public List<String> getGroundMonsterIds() {
        List<String> groundMonsterIds = new ArrayList<>(monsterIds);
        groundMonsterIds.retainAll(MonsterGenerator.getGroundMonsterIds());
        return groundMonsterIds;
    }

    public List<String> getWaterMonsterIds() {
        List<String> waterMonsterIds = new ArrayList<>(monsterIds);
        waterMonsterIds.retainAll(MonsterGenerator.getWaterMonsterIds());
        return waterMonsterIds;

    }

    public MonsterIdGroup(List<String> monsterIds, boolean canGenBoss, String bossId) {
        this.monsterIds = monsterIds;
        this.canGenBoss = canGenBoss;
        this.bossId = bossId;
    }

    // Copy/clone constructor -- this is needed since we want to read in whether
    // a boss could be generated once per program run, but need to reset the
    // canGenBoss field once per game.
    public MonsterIdGroup(MonsterIdGroup other) {
        this.monsterIds = other.monsterIds;
        this.canGenBoss = other.canGenBoss;
        this.bossId = other.bossId;
    }
}
