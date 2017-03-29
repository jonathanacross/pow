package pow.backend.dungeon;


import pow.util.Direction;
import pow.util.Point;

public class DungeonEffect {

    public enum EffectType {
        ARROW,
        SMALL_BALL,
        LARGE_BALL,
        BOLT
    }

    // This could be changed to element, if it makes sense, later
    public enum EffectColor {
        NONE,
        WHITE,
        RED,
        ORANGE,
        YELLOW,
        GREEN,
        BLUE,
        PURPLE
    }

    public static String getEffectId(
            EffectType effectType,
            EffectColor color,
            Direction dir) {

        String dirStr = dir.toString();
        String colorStr = color.toString().toLowerCase();

        String id;
        switch (effectType) {
            case ARROW: id = dirStr + " arrow"; break;
            case SMALL_BALL: id = "small " + colorStr + " ball"; break;
            case LARGE_BALL: id = "big " + colorStr + " ball"; break;
            case BOLT: id = dirStr + " " + colorStr + " bolt"; break;
            default: id = "debug";
        }

        return id;
    }

    public static DungeonObject getEffect(String id, Point point) {
        return new DungeonObject(new DungeonObject.Params(
                id, "", id, "", point, false));
    }
}
