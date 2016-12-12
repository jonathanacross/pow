package pow.frontend.effect;

public class GlyphLoc {// TODO: replace with a DungeonObject?
    private int x;
    private int y;
    private String imageName;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getImageName() {
        return imageName;
    }

    public GlyphLoc(int x, int y, String imageName) {
        this.x = x;
        this.y = y;
        this.imageName = imageName;
    }
}
