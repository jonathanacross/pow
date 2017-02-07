//package pow.frontend.utils.targeting;
//
//import pow.backend.GameMap;
//import pow.backend.GameState;
//import pow.util.Point;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class GroundTargeting implements TargetingMode {
//
//    private int radius;
//
//    public GroundTargeting(int radius) {
//        this.radius = radius;
//    }
//
//    @Override
//    public List<Point> targetableSquares(GameState gameState) {
//        Point playerLoc = gameState.player.loc;
//        GameMap map = gameState.getCurrentMap();
//
//        int rowMin = Math.max(0, playerLoc.y - radius);
//        int rowMax = Math.min(map.height - 1, playerLoc.y + radius);
//        int colMin = Math.max(0, playerLoc.x - radius);
//        int colMax = Math.min(map.width - 1, playerLoc.x + radius);
//
//        List<Point> points = new ArrayList<>();
//        for (int x = colMin; x <= colMax; x++) {
//            for (int y = rowMin; y <= rowMax; y++) {
//                if (!map.map[x][y].blockAir()) {
//                    points.add(new Point(x, y));
//                }
//            }
//        }
//        Point ploc = new Point(playerLoc.x, playerLoc.y);
//        TargetingUtils.orderPointsByDistance(ploc, points);
//
//        return points;
//    }
//}
//
