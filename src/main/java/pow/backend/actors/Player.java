package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.action.Action;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Player extends Actor implements Serializable {
    private Queue<Action> actionQueue;

    public Player(String id, String name, String image, String description, int x, int y) {
        super(id, name, image, description, x, y, true, 10, true, 0);
        this.actionQueue = new LinkedList<>();
    }

    public void addCommand(Action request) {
        this.actionQueue.add(request);
    }

    @Override
    public String getPronoun() {
        return "you";
    }

    @Override
    public boolean needsInput() {
        return actionQueue.isEmpty();
    }

    @Override
    public Action act(GameBackend backend) {
        return this.actionQueue.poll();
    }
}
