package pow.backend.dungeon;

import java.io.Serializable;
import java.util.List;

public class MonsterIdGroup implements Serializable {
    // Monsters possible to generate.  If empty, then no monsters will be
    // made.  If null, then *any* monsters may be generated (this case is
    // mostly for debugging.)
    public final List<String> monsterIds;

    // Is it possible to generate a boss?  This will be false in two cases:
    // 1. There is no boss
    // 2. The boss was already generated at some point.
    public boolean canGenBoss;

    // Boss (if any) to create for this level.  Null if no boss.
    public final String bossId;

    public MonsterIdGroup(List<String> monsterIds, boolean canGenBoss, String bossId) {
        this.monsterIds = monsterIds;
        this.canGenBoss = canGenBoss;
        this.bossId = bossId;
    }

    // copy/clone constructor -- this is needed since we want to read in whether
    // a boss could be generated once per program run, but need to reset the
    // canGenBoss field once per game.
    public MonsterIdGroup(MonsterIdGroup other) {
        this.monsterIds = other.monsterIds;
        this.canGenBoss = other.canGenBoss;
        this.bossId = other.bossId;
    }
}
