//package pow.frontend.utils.targeting;
//
//import pow.backend.GameMap;
//import pow.backend.GameState;
//import pow.backend.actors.Actor;
//import pow.util.Point;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MonsterTargeting implements TargetingMode {
//    private List<Point> points;
//
//    public MonsterTargeting(GameState gameState, int xMin, int xMax, int yMin, int yMax) {
//        GameMap map = gameState.getCurrentMap();
//        points = new ArrayList<>();
//        for (int x = xMin; x <= xMax; x++) {
//            for (int y = yMin; y <= yMax; y++) {
//                if (!gameState.player.canSee(gameState, new Point(x, y))) continue;
//                Actor a = map.actorAt(x, y);
//                if (a != null && !a.friendly) {
//                    points.add(new Point(x, y));
//                }
//            }
//        }
//        TargetingUtils.orderPointsByDistance(gameState.player.loc, points);
//    }
//
//    @Override
//    public List<Point> targetableSquares() { return this.points; }
//}
//
