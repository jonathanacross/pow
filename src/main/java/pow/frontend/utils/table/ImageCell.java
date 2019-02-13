package pow.frontend.utils.table;

import pow.frontend.Style;
import pow.frontend.utils.ImageController;

import java.awt.Graphics;

public class ImageCell implements Cell {

    String imageName;
    boolean grayed;

    public ImageCell(String imageName, boolean grayed) {
        this.imageName = imageName;
        this.grayed = grayed;
    }

    @Override
    public void draw(Graphics graphics, int x, int y, int cellWidth, int cellHeight) {
        ImageController.DrawMode drawMode = grayed ? ImageController.DrawMode.GRAY : ImageController.DrawMode.NORMAL;
        // center the image in the cell
        int dx = (cellWidth - getWidth()) / 2;
        int dy = (cellHeight - getHeight()) / 2;
        ImageController.drawTile(graphics, imageName, x + dx, y + dy, drawMode);
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
