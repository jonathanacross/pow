package pow.util;

public class Metric {

    public interface MetricFunction {
        double dist(int dx, int dy);
    }

    // a.k.a. L-infinity norm
    public static class RogueMetric implements MetricFunction {
        @Override
        public double dist(int dx, int dy) {
            return Math.max(Math.abs(dx), Math.abs(dy));
        }
    }

    public static class EuclideanMetric implements MetricFunction {
        @Override
        public double dist(int dx, int dy) {
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}