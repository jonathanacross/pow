package pow.frontend.widget;

import pow.frontend.Style;
import pow.frontend.utils.ImageController;

import java.awt.*;

public class Tile implements Widget {

    private final String tileName;
    private final State state;

    public Tile(String tileName, State state) {
        this.tileName = tileName;
        this.state = state;
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {
        ImageController.DrawMode drawMode = state == State.DISABLED ? ImageController.DrawMode.GRAY : ImageController.DrawMode.NORMAL;
        ImageController.drawTile(graphics, tileName, x, y, drawMode);
        if (state == State.SELECTED) {
            graphics.setColor(Color.YELLOW);
            graphics.drawRect(x, y, getWidth(), getHeight());
        }
    }

    @Override
    public int getHeight() {
        return Style.TILE_SIZE;
    }

    @Override
    public int getWidth() {
        return Style.TILE_SIZE;
    }
}
