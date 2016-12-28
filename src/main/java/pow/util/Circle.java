package pow.util;

import java.util.ArrayList;
import java.util.List;

public class Circle {

    // use custom radii for small circles to improve their appearance
    private static int[] RADII_SQUARED = {0, 2, 5, 10, 18, 26, 38};
    private static int MAX_CIRCLE_CACHE_SIZE = 15;

    private static List<List<Point>> CIRCLE_POINTS;

    static {
        CIRCLE_POINTS = new ArrayList<>();
        for (int i = 0; i < MAX_CIRCLE_CACHE_SIZE; i++) {
            CIRCLE_POINTS.add(computePointsInCircle(i));
        }
    }

    public static int getRadiusSquared(int radius) {
        return radius < RADII_SQUARED.length ? RADII_SQUARED[radius] : radius*radius;
    }

    public static List<Point> getPointsInCircle(int radius) {
        if (radius < MAX_CIRCLE_CACHE_SIZE) {
            return CIRCLE_POINTS.get(radius);
        } else {
            return computePointsInCircle(radius);
        }
    }

    private static List<Point> computePointsInCircle(int radius) {
        int r2 = getRadiusSquared(radius);

        List<Point> pointList = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x*x + y*y <= r2) {
                    pointList.add(new Point(x,y));
                }
            }
        }
        return pointList;
    }
}
