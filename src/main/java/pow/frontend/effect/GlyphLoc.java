package pow.frontend.effect;

import pow.util.Point;

public class GlyphLoc {// TODO: replace with a DungeonObject
    public final Point loc;
    public final String imageName;

    public GlyphLoc(int x, int y, String imageName) {
        this.loc = new Point(x,y);
        this.imageName = imageName;
    }
}
