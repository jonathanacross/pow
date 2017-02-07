package pow.util;

// Implements field of view using recursive shadowcasting.  Adapted from
// http://www.roguebasin.com/index.php?title=Improved_Shadowcasting_in_Java

import pow.util.direction.Direction;
import pow.util.direction.DirectionSets;

public class FieldOfView {
    private boolean[][] blockMap;
    private boolean[][] lightMap;
    private int startX;
    private int startY;
    private double radius;
    private Metric.MetricFunction metric;

    public FieldOfView(
            boolean[][] blockMap,
            int startX,
            int startY,
            int radius,
            Metric.MetricFunction metric) {
        this.blockMap = blockMap;
        this.startX = startX;
        this.startY = startY;
        this.radius = radius;
        this.metric = metric;

        int width = Array2D.width(blockMap);
        int height = Array2D.height(blockMap);
        this.lightMap = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                lightMap[x][y] = false;
            }
        }

        lightMap[startX][startY] = true; // light the starting cell
        for (Direction d : DirectionSets.Diagonal.getDirections()) {
            castLight(1, 1.0, 0.0, 0, d.dx, d.dy, 0);
            castLight(1, 1.0, 0.0, d.dx, 0, 0, d.dy);
        }
    }

    public boolean[][] getFOV() {
        return lightMap;
    }

    private void castLight(int row, double start, double end, int xx, int xy, int yx, int yy) {
        double newStart = 0.0;
        if (start < end) {
            return;
        }
        boolean blocked = false;
        int width = Array2D.width(blockMap);
        int height = Array2D.height(blockMap);
        for (int distance = row; distance <= radius && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startX + deltaX * xx + deltaY * xy;
                int currentY = startY + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5) / (deltaY + 0.5);
                double rightSlope = (deltaX + 0.5) / (deltaY - 0.5);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                // check if it's within the lightable area and light if needed
                if (metric.dist(deltaX, deltaY) <= radius) {
                    lightMap[currentX][currentY] = true;
                }

                if (blocked) { // previous cell was a blocking one
                    if (blockMap[currentX][currentY]) { // hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (blockMap[currentX][currentY] && distance < radius) { // hit a wall within sight line
                        blocked = true;
                        castLight(distance + 1, start, leftSlope, xx, xy, yx, yy);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }
}

