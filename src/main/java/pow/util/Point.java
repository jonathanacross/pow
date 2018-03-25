package pow.util;

import java.io.Serializable;

public class Point implements Serializable {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void shiftBy(Point other) {
        this.x += other.x;
        this.y += other.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (x != point.x) return false;
        return y == point.y;
    }

    @Override
    public int hashCode() {
        return 32768*x + y;
    }

    public Point add(Direction direction) {
        return new Point(x + direction.dx, y + direction.dy);
    }

    // Dot product, treating points as vectors.
    public int dot(Point other) {
        return this.x * other.x + this.y * other.y;
    }
}
