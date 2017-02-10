package pow.frontend.utils;


import pow.util.DebugLogger;
import pow.util.Point;
import pow.util.TsvReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private BufferedImage tileImage;
    private BufferedImage grayTileImage;
    private Map<String, Point> tileData;
    private Map<String, Color> colorData;
    public static final int TILE_SIZE = 32;

    public static void drawTile(Graphics graphics, String tileName, int x, int y) {
        drawTile(graphics, tileName, x, y, false);
    }

    public static Color getColor(String tileName) {
        if (!instance.colorData.containsKey(tileName)) {
            return Color.MAGENTA;
        }
        return instance.colorData.get(tileName);
    }


    public static void drawTile(Graphics graphics, String tileName, int x, int y, boolean gray) {
        Point srcLoc;
        if (!instance.tileData.containsKey(tileName)) {
            System.out.println("error - couldn't find tile with name '" + tileName + "'");
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

    public static void drawTile(Graphics graphics, String tileName, int x, int y, boolean gray, int size) {
        Point srcLoc;
        if (!instance.tileData.containsKey(tileName)) {
            System.out.println("error - couldn't find tile with name '" + tileName + "'");
            srcLoc = instance.tileData.get("debug");
        } else {
            srcLoc = instance.tileData.get(tileName);
        }
        BufferedImage srcImage = gray ? instance.grayTileImage : instance.tileImage;
        graphics.drawImage(srcImage, x, y, x + size, y + size,
                srcLoc.x * TILE_SIZE, srcLoc.y * TILE_SIZE,
                (srcLoc.x + 1) * TILE_SIZE, (srcLoc.y + 1) * TILE_SIZE,
                null);
    }

    private ImageController() throws IOException, DataFormatException {
        InputStream imageStream = this.getClass().getResourceAsStream("/images/32x32.png");
        this.tileImage = ImageIO.read(imageStream);
        this.grayTileImage = ImageUtils.makeGrayscale(this.tileImage);
        this.tileData = readDataFile("/data/32x32.tsv");
        this.colorData = initColorsFromTiles(this.tileImage, this.tileData, TILE_SIZE);
    }

    private Map<String, Color> initColorsFromTiles(BufferedImage tileImage, Map<String, Point> tileData, int tileSize) {

        Map<String, Color> colorData = new HashMap<>();
        for (Map.Entry<String, Point> entry : tileData.entrySet()) {
            String tileName = entry.getKey();
            Point p = entry.getValue();
            Color avgColor = ImageUtils.getAverageColorOfSubImage(tileImage, p.x*tileSize, p.y*tileSize, tileSize, tileSize);
            colorData.put(tileName, avgColor);
        }

        return colorData;
    }

    private Map<String, Point> readDataFile(String name) throws DataFormatException, IOException {
        //Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream(name);
        TsvReader reader = new TsvReader(tsvStream);

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
