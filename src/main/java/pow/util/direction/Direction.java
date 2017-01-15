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

//    static Direction[] cardinals = {new Direction(-1, 0), new Direction(1, 0), new Direction(0, -1), new Direction(0, 1)};
//    static Direction[] diagonals = {new Direction(-1, -1), new Direction(-1, 1), new Direction(1, -1), new Direction(1, 1)};
//    static Direction[] allDirs = {new Direction(-1, -1), new Direction(-1, 0), new Direction(-1, 1), new Direction(0, -1), new Direction(0, 1), new Direction(1, -1), new Direction(1, 0), new Direction(1, 1)};
}
