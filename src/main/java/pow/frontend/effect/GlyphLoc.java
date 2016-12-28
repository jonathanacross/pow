package pow.frontend.effect;

import pow.util.Point;

public class GlyphLoc {// TODO: replace with a DungeonObject
    public Point loc;
    public String imageName;

    public GlyphLoc(int x, int y, String imageName) {
        this.loc = new Point(x,y);
        this.imageName = imageName;
    }
}
