package pow.backend.action;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.dungeon.DungeonEffect;
import pow.util.FieldOfView;
import pow.util.Metric;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

// Various utility functions helpful for spells.
public class SpellUtils {

    // Returns the list of squares visible from "center",
    // at distance at most "radius", given the metric "metric".
    public static List<Point> getFieldOfView(GameState gameState, Point center, int radius, Metric.MetricFunction metric) {
        GameMap map = gameState.getCurrentMap();

        int rowMin = Math.max(0, center.y - radius);
        int rowMax = Math.min(map.height - 1, center.y + radius);
        int colMin = Math.max(0, center.x - radius);
        int colMax = Math.min(map.width - 1, center.x + radius);

        boolean[][] blockMap = new boolean[colMax - colMin + 1][rowMax - rowMin + 1];
        for (int x = colMin; x <= colMax; x++) {
            for (int y = rowMin; y <= rowMax; y++) {
                blockMap[x - colMin][y - rowMin] = map.map[x][y].blockAir();
            }
        }
        FieldOfView fov = new FieldOfView(blockMap, center.x - colMin, center.y - rowMin, radius, metric);
        boolean[][] visible = fov.getFOV();

        List<Point> points = new ArrayList<>();
        for (int x = colMin; x <= colMax; x++) {
            for (int y = rowMin; y <= rowMax; y++) {
                if (visible[x - colMin][y - rowMin] && !map.map[x][y].blockAir()) {
                    points.add(new Point(x, y));
                }
            }
        }

        return points;
    }

    public static DungeonEffect.EffectColor getEffectColor(SpellParams.Element element) {
        switch (element) {
            case NONE: return DungeonEffect.EffectColor.NONE;
            case ACID: return DungeonEffect.EffectColor.BLACK;
            case CONFUSE: return DungeonEffect.EffectColor.ORANGE;
            case DAMAGE: return DungeonEffect.EffectColor.WHITE;
            case FIRE: return DungeonEffect.EffectColor.RED;
            case ICE: return DungeonEffect.EffectColor.BLUE;
            case LIGHTNING: return DungeonEffect.EffectColor.YELLOW;
            case POISON: return DungeonEffect.EffectColor.GREEN;
            case SLEEP: return DungeonEffect.EffectColor.PURPLE;
            case STUN: return DungeonEffect.EffectColor.ORANGE;
        }
        return DungeonEffect.EffectColor.NONE;
    }
}
