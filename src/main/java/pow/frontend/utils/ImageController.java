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

    public enum DrawMode {
        NORMAL, GRAY, TRANSPARENT, COLOR_BLOCK
    }

    // big pictures
    private BufferedImage splashScreenImage;
    private BufferedImage gameOverImage;

    // tile images
    private BufferedImage tileImage;
    private BufferedImage grayTileImage;
    private BufferedImage transparentTileImage;
    private BufferedImage colorBlockImage;
    private Map<String, Point> tileData;
    private Map<String, Color> colorData;
    public static final int TILE_SIZE = 32;

    public static void drawTile(Graphics graphics, String tileName, int x, int y) {
        drawTile(graphics, tileName, x, y, DrawMode.NORMAL);
    }

    public static Color getColor(String tileName) {
        if (!instance.colorData.containsKey(tileName)) {
            return Color.MAGENTA;
        }
        return instance.colorData.get(tileName);
    }

    public static BufferedImage getSplashScreenImage() {
        return instance.splashScreenImage;
    }

    public static BufferedImage getGameOverImage() {
        return instance.gameOverImage;
    }

    private static BufferedImage getSrcImage(DrawMode drawMode)  {
        switch (drawMode) {
            case NORMAL: return instance.tileImage;
            case GRAY: return instance.grayTileImage;
            case TRANSPARENT: return instance.transparentTileImage;
            case COLOR_BLOCK: return instance.colorBlockImage;
        }
        return null;
    }

    public static void drawTile(Graphics graphics, String tileName, int x, int y, DrawMode mode) {
        Point srcLoc;
        if (!instance.tileData.containsKey(tileName)) {
            System.out.println("error - couldn't find tile with name '" + tileName + "'");
            srcLoc = instance.tileData.get("debug");
        } else {
            srcLoc = instance.tileData.get(tileName);
        }
        BufferedImage srcImage = getSrcImage(mode);
        graphics.drawImage(srcImage, x, y, x + TILE_SIZE, y + TILE_SIZE,
                srcLoc.x * TILE_SIZE, srcLoc.y * TILE_SIZE,
                (srcLoc.x + 1) * TILE_SIZE, (srcLoc.y + 1) * TILE_SIZE,
                null);
    }

    public static void drawTile(Graphics graphics, String tileName, int x, int y, DrawMode mode, int size) {
        Point srcLoc;
        if (!instance.tileData.containsKey(tileName)) {
            System.out.println("error - couldn't find tile with name '" + tileName + "'");
            srcLoc = instance.tileData.get("debug");
        } else {
            srcLoc = instance.tileData.get(tileName);
        }
        BufferedImage srcImage = getSrcImage(mode);
        graphics.drawImage(srcImage, x, y, x + size, y + size,
                srcLoc.x * TILE_SIZE, srcLoc.y * TILE_SIZE,
                (srcLoc.x + 1) * TILE_SIZE, (srcLoc.y + 1) * TILE_SIZE,
                null);
    }

    private BufferedImage readImageFromResourceFile(String name) throws IOException {
        InputStream imageStream = this.getClass().getResourceAsStream(name);
        return ImageIO.read(imageStream);
    }

    private ImageController() throws IOException, DataFormatException {
        this.splashScreenImage = readImageFromResourceFile("/images/splashscreen.png");
        this.gameOverImage = readImageFromResourceFile("/images/tombstone.png");
        this.tileImage = readImageFromResourceFile("/images/32x32.png");
        this.grayTileImage = ImageUtils.makeGrayscale(this.tileImage);
        this.transparentTileImage = ImageUtils.makeTransparent(this.tileImage);
        this.colorBlockImage = ImageUtils.makeColorBlocks(this.tileImage, TILE_SIZE);
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
