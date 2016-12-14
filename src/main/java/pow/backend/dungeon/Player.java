package pow.backend.dungeon;

import java.io.Serializable;

public class Player extends DungeonObject implements Serializable {
    public Player(String id, String name, String image, String description, int x, int y) {
        super(id, name, image, description, x, y, true);
    }
}
