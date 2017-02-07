//package pow.frontend.utils.targeting;
//
//import pow.backend.GameMap;
//import pow.backend.GameState;
//import pow.util.FieldOfView;
//import pow.util.Metric;
//import pow.util.Point;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class FOVTargeting implements TargetingMode {
//    private int radius;
//    private Metric.MetricFunction metric;
//    private Point center;
//
//    public FOVTargeting(int radius, Metric.MetricFunction metric) {
//        this.radius = radius;
//        this.metric = metric;
//        this.center = new Point(game.player.x, game.player.y);
//    }
//
//    public FOVTargeting(int radius, Metric.MetricFunction metric, Point center) {
//        this.radius = radius;
//        this.metric = metric;
//        this.center = center;
//    }
//
//    @Override
//    List<Point> targetableSquares(GameState gameState) {
//        GameMap map = gameState.getCurrentMap();
//
//        int rowMin = Math.max(0, center.y - radius);
//        int rowMax = Math.min(map.height - 1, center.y + radius);
//        int colMin = Math.max(0, center.x - radius);
//        int colMax = Math.min(map.width - 1, center.x + radius);
//
//        boolean[][] blockMap = new boolean[colMax - colMin + 1][rowMax - rowMin + 1];
//        for (int x = colMin; x <= colMax; x++) {
//            for (int y = rowMin; y <= rowMax; y++) {
//                blockMap[x - colMin][y - rowMin] = map.map[x][y].blockAir();
//            }
//        }
//        FieldOfView fov = new FieldOfView(blockMap, center.x - colMin, center.y - rowMin, radius, metric);
//        boolean[][] visible = fov.getFOV();
//
//        List<Point> points = new ArrayList<>();
//        for (int x = colMin; x <= colMax; x++) {
//            for (int y = rowMin; y <= rowMax; y++) {
//                if (visible[x - colMin][y - rowMin] && !map.map[x][y].blockAir()) {
//                    points.add(new Point(x, y));
//                }
//            }
//        }
//        TargetingUtils.orderPointsByDistance(center, points);
//
//        return points;
//    }
//}
//
