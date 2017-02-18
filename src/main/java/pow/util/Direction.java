package pow.util;

public enum Direction {
    N(0, -1, 0),
    NE(1, -1, 0),
    E(1, 0, 0),
    SE(1, 1, 0),
    S(0, 1, 0),
    SW(-1, 1, 0),
    W(-1, 0, 0),
    NW(-1, -1, 0),
    U(0, 0, 1),
    D(0, 0, -1);

    public int dx;
    public int dy;
    public int dz;
    public Direction opposite;
    public Direction rotateLeft90;
    public Direction rotateLeft45;
    public Direction rotateRight45;
    public Direction rotateRight90;

    Direction(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public static final Direction[] CARDINALS = {N, E, S, W};
    public static final Direction[] DIAGONALS = {NE, SE, NW, SW};
    public static final Direction[] ALL = {N, NE, E, SE, S, SW, W, NW};
    public static final Direction[] EXITS = {N, NE, E, SE, S, SW, W, NW, U, D};

    public static Direction getDir(int dx, int dy) {
        if (dx == 0) {
            if (dy >= 0) return S;
            else return N;
        }

        double slope = (double) dy / (double) dx;
        if (dx > 0) {
            if (slope < -2.414) return N;
            else if (slope < -0.414) return NE;
            else if (slope < 0.414) return E;
            else if (slope < 2.414) return SE;
            else return S;
        }
        else {
            if (slope < -2.414) return S;
            else if (slope < -0.414) return SW;
            else if (slope < 0.414) return W;
            else if (slope < 2.414) return NW;
            else return N;
        }
    }

    public static Direction getDir(Point src, Point dst) {
        int dx = dst.x - src.x;
        int dy = dst.y - src.y;
        return getDir(dx, dy);
    }

    // gives the direction between two points, where opposite
    // directions are identified (e.g., NE = SW).
    public static Direction getDirEquiv(Point src, Point dst) {
        int dx = dst.x - src.x;
        int dy = dst.y - src.y;

        if (dy == 0) return N;

        double slope = (double) dy / (double) dx;
        if (slope < -2.414) return N;
        else if (slope < -0.414) return SE;
        else if (slope < 0.414) return E;
        else if (slope < 2.414) return NE;
        else return N;
    }

    static {
        N.opposite = S;
        S.opposite = N;
        E.opposite = W;
        W.opposite = E;
        NE.opposite = SW;
        SW.opposite = NE;
        SE.opposite = NW;
        NW.opposite = SE;
        U.opposite = D;
        D.opposite = U;

        N.rotateLeft90 = W;
        NE.rotateLeft90 = NW;
        E.rotateLeft90 = N;
        SE.rotateLeft90 = NE;
        S.rotateLeft90 = E;
        SW.rotateLeft90 = SE;
        W.rotateLeft90 = S;
        NW.rotateLeft90 = SW;
        U.rotateLeft90 = U;
        D.rotateLeft90 = D;

        N.rotateLeft45 = NW;
        NE.rotateLeft45 = N;
        E.rotateLeft45 = NE;
        SE.rotateLeft45 = E;
        S.rotateLeft45 = SE;
        SW.rotateLeft45 = S;
        W.rotateLeft45 = SW;
        NW.rotateLeft45 = W;
        U.rotateLeft45 = U;
        D.rotateLeft45 = D;

        N.rotateRight45 = NE;
        NE.rotateRight45 = E;
        E.rotateRight45 = SE;
        SE.rotateRight45 = S;
        S.rotateRight45 = SW;
        SW.rotateRight45 = W;
        W.rotateRight45 = NW;
        NW.rotateRight45 = N;
        U.rotateRight45 = U;
        D.rotateRight45 = D;

        N.rotateRight90 = E;
        NE.rotateRight90 = SE;
        E.rotateRight90 = S;
        SE.rotateRight90 = SW;
        S.rotateRight90 = W;
        SW.rotateRight90 = NW;
        W.rotateRight90 = N;
        NW.rotateRight90 = NE;
        U.rotateRight90 = U;
        D.rotateRight90 = D;
    }

}
