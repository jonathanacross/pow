package pow.util;

import java.util.ArrayList;
import java.util.List;

public class Arc {

    public static List<Point> createArc(Point start, Point end) {
        final int height = 5;
        final int maxSteps = 20;

        List<Point> points = new ArrayList<>();

        double dx = end.x - start.x;
        double dy = end.y - start.y;
        int numSteps = (int) Math.min(2*Math.max(Math.abs(dx), Math.abs(dy)), maxSteps);

        for (int i = 0; i <= numSteps; i++) {
            double t = (double) i / numSteps;
            double x = t * start.x + (1.0 - t) * end.x;
            double y = t * start.y + (1.0 - t) * end.y;
            double z = 4.0 * height * t * (1.0 - t);

            points.add(new Point((int) Math.round(x), (int) Math.round(y - z)));
        }
        return points;
    }
}
