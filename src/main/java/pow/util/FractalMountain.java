package pow.util;

import java.io.PrintWriter;
import java.util.Random;

// Class to generate a fractal mountain of size 2^k + 1.
// This is loosely based on ideas from http://www.playfuljs.com/realistic-terrain-in-130-lines/
// This works by recursive subdivision.  Each new interior point is set as some combination
// of its neighbors.  Say the neighbors have values (a,b,c,d).  Then the new point will
// have value of alpha * randomUniform( min(a,b,c,d), max(a,b,c,d)) + (1 - alpha) * mean(a,b,c,d).
// Larger values of alpha make the terrain more random, smaller make it smoother.
// This mountain is set up to have values 0 around the borders and 1 near the center.
public class FractalMountain {
    private final int size;
    private final double roughness; // between 0 and 1; 1 is fully rough, 0 is perfectly smooth.
    private final double[][] data;

    private final double OUTSIDE_RANGE = Double.MAX_VALUE;

    // parameters: detail impacts the size: size of map will be 2^detail + 1;
    // roughness should be between 0 and 1; 1 is fully rough, 0 is perfectly smooth.
    public FractalMountain(int detail, double roughness) {
        size = (1 << detail) + 1;
        data = new double[size][size];
        this.roughness = roughness;
    }

    public double[][] generateHeights(Random rng) {
        // Set up so there's a peak in the middle and valleys around the perimeter.
        int max = size - 1;
        int middle = max / 2;
        setp(0, 0, 0);
        setp(max, 0, 0);
        setp(max, max, 0);
        setp(0, max, 0);
        setp(middle, middle, 1.0);
        setp(0, middle, 0.0);
        setp(max, middle, 0.0);
        setp(middle, 0, 0.0);
        setp(middle, max, 0.0);

        divide(rng);

        return data;
    }

    private void divide(Random rng) {
        int delta = (size - 1) / 2;
        int middle = delta / 2;

        while (middle >= 1) {
            for (int y = middle; y < size - 1; y += delta) {
                for (int x = middle; x < size - 1; x += delta) {
                    double value = interpolateSquare(x, y, middle, roughness, rng);
                    setp(x, y, value);
                }
            }

            for (int y = 0; y < size; y += middle) {
                for (int x = (y + middle) % delta; x < size; x += delta) {
                    double value = interpolateDiamond(x, y, middle, roughness, rng);
                    setp(x, y, value);
                }
            }
            delta /= 2;
            middle /= 2;
        }
    }

    private double interpolateSquare(int x, int y, int delta, double roughness, Random rng) {
        return interpolate(new double[]{
                getp(x - delta, y - delta), // upper left
                getp(x + delta, y - delta), // upper right
                getp(x + delta, y + delta), // lower right
                getp(x - delta, y + delta)  // lower left
        }, roughness, rng);
    }

    private double interpolateDiamond(int x, int y, int delta, double roughness, Random rng) {
        return interpolate(new double[]{
                getp(x, y - delta), // top
                getp(x + delta, y), // right
                getp(x, y + delta), // bottom
                getp(x - delta, y)  // left
        }, roughness, rng);
    }

    private double interpolate(double[] values, double roughness, Random rng) {
        int count = values.length;
        double total = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double value : values) {
            if (value != OUTSIDE_RANGE) {
                total += value;
                min = Math.min(value, min);
                max = Math.max(value, max);
            } else {
                min = Math.min(0, min);
            }
        }
        double average = total / count;
        return roughness * uniformRandom(rng, min, max) + (1 - roughness) * average;
    }

    private double uniformRandom(Random rng, double lo, double hi) {
        return rng.nextDouble() * (hi - lo) + lo;
    }

    private double getp(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return OUTSIDE_RANGE;
        }
        return data[x][y];
    }

    private void setp(int x, int y, double val) {
        data[x][y] = val;
    }

    public static void main(String[] args) {
        FractalMountain fm = new FractalMountain(6, 0.5);
        Random rng = new Random();
        double[][] heights = fm.generateHeights(rng);

        try (PrintWriter writer = new PrintWriter("/Users/jonathan/Development/pow/z.txt", "UTF-8")) {
            for (int r = 0; r < heights.length; r++) {
                for (int c = 0; c < heights[0].length; c++) {
                    if (c > 0) {
                        writer.print("\t");
                    }
                    writer.print(heights[r][c]);
                }
                writer.println();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

