package pow.backend.dungeon;

import pow.util.Point;

public class SimpleLightSource implements LightSource {
    private Point location;
    public int radius;

    public SimpleLightSource(Point location, int radius) {
        this.location = location;
        this.radius = radius;
    }

    @Override
    public Point getLocation() { return location; }

    @Override
    public int getLightRadius() { return radius; }
}
