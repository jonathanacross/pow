//package pow.frontend.utils.targeting;
//
//import pow.backend.GameMap;
//import pow.backend.GameState;
//import pow.util.Metric;
//import pow.util.Point;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class MonsterFOVTargeting implements TargetingMode {
//    int radius;
//    Metric.MetricFunction metric;
//    Point center;
//
//    public MonsterFOVTargeting(int radius, Metric.MetricFunction metric) {
//        this.radius = radius;
//        this.metric = metric;
//        this.center = new Point(game.player.x, game.player.y);
//    }
//
//    public MonsterFOVTargeting(int radius, Metric.MetricFunction metric, Point center) {
//        this.radius = radius;
//        this.metric = metric;
//        this.center = center;
//    }
//
//    @Override
//    public List<Point> targetableSquares(GameState gameState) {
//        GameMap map = gameState.getCurrentMap();
//
//        List<Point> squares = (new FOVTargeting(this.radius, this.metric, this.center)).targetableSquares();
//        List<Point> msquares = new ArrayList<>();
//        for (Point square : squares) {
//            if (map.actorAt(square.x, square.y) != null) {
//                msquares.add(square);
//            }
//        }
//        TargetingUtils.orderPointsByDistance(center, msquares);
//
//        return msquares;
//    }
//}
//
