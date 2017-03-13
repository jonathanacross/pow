package pow.util;

public class Point3D {
    public final int x;
    public final int y;
    public final int z;

    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D plus(Direction dir) {
        int x = this.x + dir.dx;
        int y = this.y + dir.dy;
        int z = this.z + dir.dz;
        return new Point3D(x, y, z);
    }

    public int distSquared(Point3D other) {
        int deltaX = this.x - other.x;
        int deltaY = this.y - other.y;
        int deltaZ = this.z - other.z;
        return deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point3D point3D = (Point3D) o;

        if (x != point3D.x) return false;
        if (y != point3D.y) return false;
        return z == point3D.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}
