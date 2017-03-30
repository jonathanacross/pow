package pow.backend.dungeon;


import pow.util.Direction;
import pow.util.Point;

import java.util.Collections;
import java.util.List;

public class DungeonEffect {

    public enum EffectType {
        ARROW,
        SMALL_BALL,
        LARGE_BALL,
        BOLT
    }

    // This could be changed to element, if it makes sense
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

    public static class ImageLoc {
        public final String imageName;
        public final Point loc;

        public ImageLoc(String name, Point loc) {
            this.imageName = name;
            this.loc = loc;
        }
    }

    public final List<ImageLoc> imageLocs;
    public DungeonEffect(List<ImageLoc> imageLocs) {
        this.imageLocs = imageLocs;
    }

    // convenience constructor if the effect is only one point
    public DungeonEffect(String name, Point loc) {
        this(Collections.singletonList(new ImageLoc(name, loc)));
    }

    public static String getEffectName(
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
}
