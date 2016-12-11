package pow.frontend.utils;


import pow.util.Point;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

public class ImageController {

    private static final ImageController instance;

    static {
        try {
            instance = new ImageController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage tileImage;
    private BufferedImage grayTileImage;
    private Map<String, Point> tileData;
    public static final int TILE_SIZE = 32;

    public static void drawTile(Graphics graphics, String tileName, int x, int y) {
        drawTile(graphics, tileName, x, y, false);
    }

    public static void drawTile(Graphics graphics, String tileName, int x, int y, boolean gray) {
        Point srcLoc;
        if (!instance.tileData.containsKey(tileName)) {
            srcLoc = instance.tileData.get("debug");
        } else {
            srcLoc = instance.tileData.get(tileName);
        }
        BufferedImage srcImage = gray ? instance.grayTileImage : instance.tileImage;
        graphics.drawImage(srcImage, x, y, x + TILE_SIZE, y + TILE_SIZE,
                srcLoc.x * TILE_SIZE, srcLoc.y * TILE_SIZE,
                (srcLoc.x + 1) * TILE_SIZE, (srcLoc.y + 1) * TILE_SIZE,
                null);
    }

    private ImageController() throws IOException, DataFormatException {
        InputStream imageStream = ImageController.class.getResourceAsStream("/images/32x32.png");
        this.tileImage = ImageIO.read(imageStream);
        this.grayTileImage = ImageUtils.makeGrayscale(this.tileImage);
        this.tileData = readDataFile("/data/32x32.txt");
    }

    private Map<String, Point> readDataFile(String name) throws DataFormatException, IOException {
        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        File file = new File(ImageController.class.getResource(name).getFile());
        TsvReader reader = new TsvReader(file);

        Map<String, Point> tileMap = new HashMap<>();
        for (String[] line : reader.getData()) {
            if (line.length != 5) {
                // TODO: improve error handling
                throw new DataFormatException("expected line to have 5 fields");
            }

            int x = Integer.parseInt(line[1]);
            int y = Integer.parseInt(line[2]);
            String tileName = line[3];
            tileMap.put(tileName, new Point(x, y));
        }
        return tileMap;
    }
}
