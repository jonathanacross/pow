package pow.util;

import java.util.ArrayList;
import java.util.List;

public class Bresenham {

    // builds a ray starting at 'start', towards the point 'through', with length 'length'
    // Uses Bresenham line algorithm.
    public static List<Point> makeRay(Point start, Point through, int length) {

        List<Point> line = new ArrayList<>();

        int dx = Math.abs(through.x - start.x);
        int dy = Math.abs(through.y - start.y);

        int sx = start.x < through.x ? 1 : -1;
        int sy = start.y < through.y ? 1 : -1;

        int err = dx - dy;
        int e2;
        int currentX = start.x;
        int currentY = start.y;

        for (int len = 0; len < length; len += 1) {
            line.add(new Point(currentX, currentY));

            e2 = 2 * err;
            if (e2 > -1 * dy) {
                err = err - dy;
                currentX = currentX + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                currentY = currentY + sy;
            }
        }

        return line;
    }
}
