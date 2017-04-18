package pow.util;

public class Metric {

    public interface MetricFunction {
        double dist(int dx, int dy);
    }

    public static final MetricFunction rogueMetric = (int dx, int dy) -> Math.max(Math.abs(dx), Math.abs(dy));
    public static final MetricFunction euclideanMetric = (int dx, int dy) -> Math.sqrt(dx * dx + dy * dy);
}