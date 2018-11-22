package pow.util;

public class MathUtils {
    public static int clamp(int value, int low, int hi) {
        return Math.max(Math.min(value, hi), low);
    }

    public static double clamp(double value, double low, double hi) {
        return Math.max(Math.min(value, hi), low);
    }

    public static int dist2(int x1, int y1, int x2, int y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static int dist2(Point p1, Point p2) {
        return dist2(p1.x, p1.y, p2.x, p2.y);
    }

    public static int sign(int x) {
        return Integer.compare(x, 0);
    }

    public static int roundEven(int n) { return (n % 2 == 0) ? n : n - 1; }
}

