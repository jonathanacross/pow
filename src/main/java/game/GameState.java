package game;

/**
 * Created by jonathan on 9/23/16.
 */
public class GameState {
    public int x = 50;
    public int y = 50;

    public void moveRight() {
        x = x + 5;
    }

    public void moveLeft() {
        x = x - 5;
    }

    public void moveDown() {
        y = y + 5;
    }

    public void moveUp() {
        y = y - 5;
    }
}
