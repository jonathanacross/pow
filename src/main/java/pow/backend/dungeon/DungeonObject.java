package pow.backend.dungeon;

import java.io.Serializable;

public class DungeonObject implements Serializable {
    public String id;   // program id, e.g., "axe"
    public String name; // english name, e.g., "& axe~"
    public String image; // for display
    public String description;
    public int x;
    public int y;
    public boolean solid;

    public DungeonObject(String id, String name, String image, String description, int x, int y, boolean solid) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.x = x;
        this.y = y;
        this.solid = solid;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
