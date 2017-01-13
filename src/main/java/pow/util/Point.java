package pow.util;

import java.io.Serializable;

public class Point implements Serializable{
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
}
