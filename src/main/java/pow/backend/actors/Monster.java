package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.command.Attack;
import pow.backend.command.CommandRequest;
import pow.backend.command.Move;
import pow.backend.command.Rest;
import pow.backend.event.GameEvent;
import pow.util.MathUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Monster extends Actor implements Serializable {

    public Monster(String id, String name, String image, String description, int maxHealth, int speed, int x, int y) {
        super(id, name, image, description, x, y, true, false, maxHealth, false, speed);
    }

    public static Monster makeRat(int x, int y) {
        return new Monster("white rat", "white rat", "white rat", "white rat", 3, 0, x, y);
    }

    public static Monster makeBat(int x, int y) {
        return new Monster("bat", "bat", "bat", "bat", 2, 2, x, y);
    }

    public static Monster makeSnake(int x, int y) {
        return new Monster("yellow snake", "yellow snake", "yellow snake", "yellow snake", 4, -2, x, y);
    }

    public String getPronoun() {
        return "the " + this.name;
    }


    public CommandRequest act(GameBackend backend) {
        GameState gs = backend.getGameState();

        // try to attack first
        Actor closestEnemy = AiUtils.findNearestTarget(this, gs);
        if (closestEnemy != null && MathUtils.dist2(x, y, closestEnemy.x, closestEnemy.y) <= 2) {
            return new Attack(this, closestEnemy);
        }

        // move randomly
        return AiUtils.wander(this, gs);
    }
}
