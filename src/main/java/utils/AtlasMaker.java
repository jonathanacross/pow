package utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class AtlasMaker {
    public static class AtlasEntry {
        public String source;
        public int x;
        public int y;
        public String name;
        public String metadata;

        @Override
        public String toString() {
            return (source + "\t" + x + "\t" + y + "\t" + name + "\t" + metadata);
        }

        public AtlasEntry(String input) {
            String[] fields = input.split("\t", 5);
            this.source = fields[0];
            this.x = Integer.parseInt(fields[1]);
            this.y = Integer.parseInt(fields[2]);
            this.name = fields[3];
            this.metadata = fields[4];
        }

        public AtlasEntry(String source, Integer x, Integer y, String name,
                          String metadata) {
            this.source = source;
            this.x = x;
            this.y = y;
            this.name = name;
            this.metadata = metadata;
        }
    }

    private static List<AtlasEntry> readAtlas(InputStream stream)
            throws IOException {
        List<AtlasEntry> entries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = br.readLine()) != null) {
                AtlasEntry entry = new AtlasEntry(line);
                entries.add(entry);
            }
        }

        return (entries);
    }

    private static List<AtlasEntry> readAtlas(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        return readAtlas(is);
    }

    public static void main(String[] args) throws IOException {
        // Parse command line
        if (args.length < 3) {
            System.out.println("Usage: input.tsv width output");
            System.out.println("  input.tsv is the input atlas, with columns source/x/y/name/metadata");
            System.out.println("  width is the number of tiles wide");
            System.out.println("  output is the base name, will generate output.png, output.tsv");
            return;
        }

        File inputAtlasFile = new File(args[0]);
        if (!inputAtlasFile.exists()) {
            System.out.println("error: " + inputAtlasFile + " doesn't exist");
            return;
        }
        int width = Integer.parseInt(args[1]);
        String output = args[2];
        int tileSize = 32;

        List<AtlasEntry> atlas = readAtlas(inputAtlasFile);
        int height = (int) Math.ceil((double) atlas.size() / width);
        int destx = 0;
        int desty = 0;

        BufferedImage outImg = new BufferedImage(width * tileSize, height
                * tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = outImg.getGraphics();
        List<AtlasEntry> newAtlas = new ArrayList<>();

        for (AtlasEntry entry : atlas) {
            System.out.println("drawing " + entry.toString());

            // draw the tile
            if (!entry.source.equals(":null:")) {
                BufferedImage inputImg = ImageIO.read(new File(entry.source + ".png"));
                BufferedImage tile = inputImg.getSubimage(entry.x * tileSize,
                        entry.y * tileSize, tileSize, tileSize);
                graphics.drawImage(tile, destx * tileSize, desty * tileSize,
                        null);
            }

            // add to the new atlas
            AtlasEntry newEntry = new AtlasEntry(output, destx, desty,
                    entry.name, entry.metadata);
            newAtlas.add(newEntry);

            destx++;
            if (destx == width) {
                destx = 0;
                desty++;
            }
        }

        // save final image
        File outputImage = new File(output + ".png");
        ImageIO.write(outImg, "png", outputImage);

        // save final atlas
        PrintWriter writer = new PrintWriter(output + ".tsv", "UTF-8");
        for (AtlasEntry entry : newAtlas) {
            writer.println(entry.toString());
        }
        writer.close();

        System.out.println("done!");

    }
}
