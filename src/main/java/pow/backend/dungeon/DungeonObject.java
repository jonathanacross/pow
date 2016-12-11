package pow.backend.dungeon;

public class DungeonObject {
    public String name;
    public String image;
    public String description;
    public int x;
    public int y;
    public boolean solid;

    public DungeonObject(String name, String image, String description, int x, int y, boolean solid) {
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
