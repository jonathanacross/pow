package pow.frontend;

// stores dimension information of windows
public class WindowDim {
    public int x;  // location of left side of window
    public int y;  // location of top side of window
    public int width;
    public int height;

    public WindowDim(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static WindowDim center(int windowWidth, int windowHeight, int screenWidth, int screenHeight) {
        int x = (screenWidth - windowWidth) / 2;
        int y = (screenHeight - windowHeight) / 3; // don't vertically center perfectly; weight toward the top
        int width = windowWidth;
        int height = windowHeight;
        return new WindowDim(x, y, width, height);
    }
}
