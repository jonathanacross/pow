package pow.util.direction;

public class Direction {
    public int dx;
    public int dy;

    public Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public double norm() {
        return Math.sqrt(dx*dx + dy*dy);
    }

    public Direction opposite() { return new Direction(-dx, -dy); }

    public static Direction N =  new Direction(0, -1);
    public static Direction NE = new Direction(1, -1);
    public static Direction E =  new Direction(1, 0);
    public static Direction SE = new Direction(1, 1);
    public static Direction S =  new Direction(0, 1);
    public static Direction SW = new Direction(-1, 1);
    public static Direction W =  new Direction(-1, 0);
    public static Direction NW = new Direction(-1, -1);

//    public static Direction[] CARDINALS = {N, E, S, W};
//    public static Direction[] DIAGONALS = {NE, SE, SW, NW};
//    public static Direction[] ALL = {N, NE, E, SE, S, SW, W, NW};
}
