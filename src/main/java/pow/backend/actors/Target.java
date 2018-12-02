package pow.backend.actors;

import pow.backend.GameState;
import pow.util.Point;

import java.io.Serializable;

public class Target implements Serializable {
    private Point floorTarget;
    private Actor monsterTarget;
    private Actor previousMonsterTarget;

    public Target() {
        clear();
    }

    public Point get() {
        if (floorTarget != null) {
            return floorTarget;
        } else if (monsterTarget != null) {
            return monsterTarget.loc;
        } else {
            return null;
        }
    }

    public void setMonster(Actor a) {
        floorTarget = null;
        monsterTarget = a;
    }

    public void setFloor(Point loc) {
        monsterTarget = null;
        floorTarget = loc;
    }

    public void clear() {
        floorTarget = null;
        monsterTarget = null;
    }

    // Checks to see if the monster target is still valid,
    // or restores the previous target if it becomes valid again.
    public void update(GameState gs, Actor actor) {
        if (monsterTarget != null) {
            // Monster is gone (e.g., it died), or it's outside field of view
            if (!gs.getCurrentMap().actors.contains(monsterTarget) ||
                    !actor.canSeeLocation(gs, monsterTarget.loc)) {
                previousMonsterTarget = monsterTarget;
                monsterTarget = null;
            }
        } else if (previousMonsterTarget != null) {
            if (gs.getCurrentMap().actors.contains(previousMonsterTarget) &&
                    actor.canSeeLocation(gs, previousMonsterTarget.loc)) {
                monsterTarget = previousMonsterTarget;
            }
        }
    }
}
