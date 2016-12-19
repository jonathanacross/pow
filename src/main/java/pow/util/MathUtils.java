package pow.util;

public class MathUtils {
    public static int clamp(int x, int low, int hi) {
        return Math.max(Math.min(x, hi), low);
    }

    public static int dist2(int x1, int y1, int x2, int y2) {
        return (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2);
    }
}
