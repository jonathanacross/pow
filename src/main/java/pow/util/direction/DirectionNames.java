package pow.util.direction;

import pow.util.Point;

// TODO: consider changing to an enum, or enum + class
public class DirectionNames {
    public static final int N = 0;
    public static final int NE = 1;
    public static final int E = 2;
    public static final int SE = 3;
    public static final int S = 4;
    public static final int SW = 5;
    public static final int W = 6;
    public static final int NW = 7;


    private static final int[] OPPOSITES = {4, 5, 6, 7, 0, 1, 2, 3};
    public static int getOpposite(int dirName) {
        return OPPOSITES[dirName];
    }

    // must match above order
    private static final Direction[] DIRECTIONS = {
            new Direction(0,-1),
            new Direction(1, -1),
            new Direction(0,1),
            new Direction(1,1),
            new Direction(0,1),
            new Direction(-1, 1),
            new Direction(-1,0),
            new Direction(-1,-1)
    };
    public static Direction getDirection(int dirName) {
        return DIRECTIONS[dirName];
    }

    public static final int[] CARDINALS = {N, E, S, W};
    public static final int[] DIAGONALS = {NE, SE, NW, SW};
    public static final int[] ALL_DIRECTIONS = {N, NE, E, SE, S, SW, W, NW};

    static int getDirName(Point src, Point dst) {
        int dx = dst.x - src.x;
        int dy = dst.y - src.y;

        if (dx == 0) {
            if (dy >= 0) return DirectionNames.S;
            else return DirectionNames.N;
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

    // gives the direction between two points, where opposite
    // directions are identified (e.g., NE = SW).
    static int getDirNameEquiv(Point src, Point dst) {
        int dx = dst.x - src.x;
        int dy = dst.y - src.y;

        if (dy == 0) return DirectionNames.N;

        double slope = (double) dy / (double) dx;
        if (slope < -2.414) return N;
        else if (slope < -0.414) return SE;
        else if (slope < 0.414) return E;
        else if (slope < 2.414) return NE;
        else return N;
    }
}