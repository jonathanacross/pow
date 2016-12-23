package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Attack;
import pow.backend.action.Action;
import pow.util.MathUtils;

import java.io.Serializable;

public class Monster extends Actor implements Serializable {

    public Monster(String id, String name, String image, String description, int maxHealth, int speed, int x, int y) {
        super(id, name, image, description, x, y, true, maxHealth, false, speed);
        this.dx = 1;
    }


    public static Monster makeRat(int x, int y) {
        return new Monster("white rat", "white rat", "white rat", "white rat", 3, 0, x, y);
    }

    public static Monster makeBat(int x, int y) { return new Monster("bat", "bat", "bat", "bat", 2, 3, x, y); }

    public static Monster makeSnake(int x, int y) {
        return new Monster("yellow snake", "yellow snake", "yellow snake", "yellow snake", 4, -3, x, y);
    }

    @Override
    public String getPronoun() {
        return "the " + this.name;
    }

    @Override
    public boolean needsInput() {
        return false;
    }

    private int dx;
    private int n;

    @Override
    public Action act(GameBackend backend) {
        GameState gs = backend.getGameState();

//        n++;
//        if (n % 2 == 0) {
//            return new FireRocket(this);
//        } else {
//            if (gs.map.isBlocked(x + 1, y) && gs.map.isBlocked(x - 1, y)) {
//                return new Move(this, 0, 0);
//            }
//            if (gs.map.isBlocked(x + dx, this.y)) {
//                dx *= -1;
//            }
//            return new Move(this, dx, 0);
//        }

        // try to attack first
        Actor closestEnemy = AiUtils.findNearestTarget(this, gs);
        if (closestEnemy != null && MathUtils.dist2(x, y, closestEnemy.x, closestEnemy.y) <= 2) {
            return new Attack(this, closestEnemy);
        }

        // move randomly
        return AiUtils.wander(this, gs);
    }
}
