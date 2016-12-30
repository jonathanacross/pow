package pow.backend.dungeon;

import pow.util.Point;

public interface LightSource {
    Point getLocation();
    int getLightRadius();
}
