package pow.frontend.utils.table;

import pow.frontend.Style;
import pow.frontend.utils.ImageController;

import java.awt.Graphics;

// TODO: change Image to Tile.
public class ImageCell implements Cell {

    String imageName;
    boolean grayed;

    public ImageCell(String imageName, boolean grayed) {
        this.imageName = imageName;
        this.grayed = grayed;
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {
        ImageController.DrawMode drawMode = grayed ? ImageController.DrawMode.GRAY : ImageController.DrawMode.NORMAL;
        ImageController.drawTile(graphics, imageName, x, y, drawMode);
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
