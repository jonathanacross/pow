package pow.backend.dungeon.gen.worldgen;

import pow.util.Direction;
import pow.util.Point3D;

public class MapConnection {
    public Point3D fromLoc;
    public Direction dir;

    public MapConnection(Point3D fromLoc, Direction dir) {
        this.fromLoc = fromLoc;
        this.dir = dir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapConnection that = (MapConnection) o;

        if (!fromLoc.equals(that.fromLoc)) return false;
        return dir == that.dir;
    }

    @Override
    public int hashCode() {
        int result = fromLoc.hashCode();
        result = 31 * result + dir.hashCode();
        return result;
    }
}
