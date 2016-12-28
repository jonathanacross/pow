package pow.util;

import java.io.Serializable;

public class Point implements Serializable{
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
