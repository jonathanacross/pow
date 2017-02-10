package pow.backend.dungeon;

import pow.util.Point;

import java.io.Serializable;

public class SimpleLightSource implements LightSource, Serializable {
    private Point location;
    private int radius;

    public SimpleLightSource(Point location, int radius) {
        this.location = location;
        this.radius = radius;
    }

    @Override
    public Point getLocation() { return location; }

    @Override
    public int getLightRadius() { return radius; }
}
