package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.command.CommandRequest;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Player extends Actor implements Serializable {
    // TODO: clarify distinction between requests and actions
    private Queue<CommandRequest> requests;

    public Player(String id, String name, String image, String description, int x, int y) {
        super(id, name, image, description, x, y, true, true, 10, true, 0);
        this.requests = new LinkedList<>();
    }

    public void addCommand(CommandRequest request) {
        this.requests.add(request);
    }

    public String getPronoun() {
        return "you";
    }

    @Override
    public CommandRequest act(GameBackend backend) {
        return this.requests.poll();
    }
}
