package pow.frontend.effect;

public class GlyphLoc {
    private int x;
    private int y;
    private char c;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public char getC() {
        return c;
    }

    public GlyphLoc(int x, int y, char c) {
        this.x = x;
        this.y = y;
        this.c = c;
    }
}
