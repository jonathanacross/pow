package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Attack;
import pow.backend.action.Action;
import pow.util.MathUtils;

import java.io.Serializable;

public class Monster extends AiActor implements Serializable {

    public Monster(String id, String name, String image, String description, int maxHealth, int speed, int x, int y, AiActor.Flags flags) {
        super(id, name, image, description, x, y, true, maxHealth, false, speed, flags);
    }

    public static Monster makeMushroom(int x, int y) {
        return new Monster("yellow mushrooms", "yellow mushrooms", "yellow mushrooms", "yellow mushrooms", 3, 0, x, y,
                new Flags(true, false));
    }

    public static Monster makeRat(int x, int y) {
        return new Monster("white rat", "white rat", "white rat", "white rat", 3, 0, x, y,
                new Flags(false, false));
    }

    public static Monster makeBat(int x, int y) { return new Monster("bat", "bat", "bat", "bat", 2, 3, x, y,
            new Flags(false, false)); }

    public static Monster makeSnake(int x, int y) {
        return new Monster("yellow snake", "yellow snake", "yellow snake", "yellow snake", 4, -3, x, y,
                new Flags(false, true));
    }

    @Override
    public String getPronoun() {
        return "the " + this.name;
    }

    @Override
    public boolean needsInput() {
        return false;
    }

//    @Override
//    public Action act(GameBackend backend) {
//        GameState gs = backend.getGameState();
//
//        // try to attack first
//        Actor closestEnemy = AiUtils.findNearestTarget(this, gs);
//        if (closestEnemy != null && MathUtils.dist2(loc, closestEnemy.loc) <= 2) {
//            return new Attack(this, closestEnemy);
//        }
//
//        // move randomly
//        return AiUtils.wander(this, gs);
//    }
}
