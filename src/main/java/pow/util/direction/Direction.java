package pow.util.direction;

public class Direction {
    public int dx;
    public int dy;

    public Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static Direction N =  new Direction(0, -1);
    public static Direction NE = new Direction(1, -1);
    public static Direction E =  new Direction(1, 0);
    public static Direction SE = new Direction(1, 1);
    public static Direction S =  new Direction(0, 1);
    public static Direction SW = new Direction(-1, 1);
    public static Direction W =  new Direction(-1, 0);
    public static Direction NW = new Direction(-1, -1);
}
