//package pow.frontend.utils.targeting;
//
//import pow.backend.GameMap;
//import pow.backend.GameState;
//import pow.backend.dungeon.DungeonFeature;
//import pow.backend.dungeon.DungeonSquare;
//import pow.util.Point;
//import pow.util.direction.Direction;
//import pow.util.direction.DirectionSets;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class AdjacentDoorTargeting implements TargetingMode {
//
//    public AdjacentDoorTargeting() {}
//
//    @Override
//    public List<Point> targetableSquares(GameState gameState) {
//        Point playerLoc = gameState.player.loc;
//        GameMap map = gameState.getCurrentMap();
//
//        List<Point> points = new ArrayList<>();
//        for (Direction dir : DirectionSets.All.getDirections()) {
//            int x = playerLoc.x + dir.dx;
//            int y = playerLoc.y + dir.dy;
//            DungeonSquare sq = map.map[x][y];
//            DungeonFeature f = sq.feature;
//            if (sq.feature != null && sq.feature.flags.isOpenDoor) {
//                points.add(new Point(x, y));
//            }
//        }
//        Point ploc = new Point(playerLoc.x, playerLoc.y);
//        TargetingUtils.orderPointsByDistance(ploc, points);
//
//        return points;
//    }
//}
//
