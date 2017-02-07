package pow.frontend.utils.targeting;

import pow.backend.GameState;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class LookTargeting implements TargetingMode {

    private List<Point> points;

    public LookTargeting(GameState gameState, int xMin, int xMax, int yMin, int yMax) {
        Point playerLoc = gameState.player.loc;
        points = new ArrayList<>();

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                points.add(new Point(x, y));
            }
        }
        Point ploc = new Point(playerLoc.x, playerLoc.y);
        TargetingUtils.orderPointsByDistance(ploc, points);
    }

    @Override
    public List<Point> targetableSquares() { return this.points; }
}

