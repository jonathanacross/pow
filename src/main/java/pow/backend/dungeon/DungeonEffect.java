package pow.backend.dungeon;


import pow.util.Direction;
import pow.util.Point;

import java.util.ArrayList;
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

    // convenience constructor if all effects are the same image
    public DungeonEffect(String name, List<Point> locs) {
        this(makeImageLocs(name, locs));
    }

    private static List<ImageLoc> makeImageLocs(String name, List<Point> locs) {
        List<ImageLoc> imageLocs = new ArrayList<>();
        for (Point loc: locs) {
            imageLocs.add(new ImageLoc(name, loc));
        }
        return imageLocs;
    }

    // used for bolts, since there are 4 symmetric images
    private static Direction getEquivDirection(Direction dir) {
        switch (dir) {
            case S: return Direction.N;
            case SW: return Direction.NE;
            case W: return Direction.E;
            case NW: return Direction.SE;
            default: return dir;
        }
    }

    public static String getEffectName(
            EffectType effectType,
            EffectColor color,
            Direction dir) {

        String colorStr = color.toString().toLowerCase();

        String id;
        switch (effectType) {
            case ARROW: id = dir.toString() + " arrow"; break;
            case SMALL_BALL: id = "small " + colorStr + " ball"; break;
            case LARGE_BALL: id = "big " + colorStr + " ball"; break;
            case BOLT: id = getEquivDirection(dir).toString() + " " + colorStr + " bolt"; break;
            default: id = "debug";
        }

        return id;
    }
}
