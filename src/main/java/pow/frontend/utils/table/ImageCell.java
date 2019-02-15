package pow.frontend.utils.table;

import pow.frontend.Style;
import pow.frontend.utils.ImageController;

import java.awt.*;

// TODO: change Image to Tile.
public class ImageCell implements Cell {

    String imageName;
    State state;

    public ImageCell(String imageName, State state) {
        this.imageName = imageName;
        this.state = state;
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {
        ImageController.DrawMode drawMode = state == State.DISABLED ? ImageController.DrawMode.GRAY : ImageController.DrawMode.NORMAL;
        ImageController.drawTile(graphics, imageName, x, y, drawMode);
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
