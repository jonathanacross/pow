package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.command.CommandRequest;
import pow.backend.dungeon.DungeonObject;
import pow.backend.event.GameEvent;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Player extends Actor implements Serializable {
    // TODO: clarify distinction between requests and actions
    private Queue<CommandRequest> requests;

    public Player(String id, String name, String image, String description, int x, int y) {
        super(id, name, image, description, x, y, true, true);
        this.requests = new LinkedList<>();
    }

    public void addCommand(CommandRequest request) {
        this.requests.add(request);
    }

    @Override
    public List<GameEvent> act(GameBackend backend) {
        if (!requests.isEmpty()) {
            return requests.poll().process(backend);
        } else {
            return null;  // TODO: or empty list?
        }
    }
}
