package pow.util.direction;

public class Direction3D {
    int dx;
    int dy;
    int dz;

    public Direction3D(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public static Direction3D U = new Direction3D(0,0,-1);
    public static Direction3D D = new Direction3D(0,0,1);
    public static Direction3D N = new Direction3D(0,-1,0);
    public static Direction3D NE = new Direction3D(1,-1,0);
    public static Direction3D E = new Direction3D(1,0,0);
    public static Direction3D SE = new Direction3D(1,1,0);
    public static Direction3D S = new Direction3D(0,1,0);
    public static Direction3D SW = new Direction3D(-1,1,0);
    public static Direction3D W = new Direction3D(-1,0,0);
    public static Direction3D NW = new Direction3D(-1,-1,0);
}

