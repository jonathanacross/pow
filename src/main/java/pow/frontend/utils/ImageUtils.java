package pow.frontend.utils;

import java.awt.image.BufferedImage;

public class ImageUtils {
    // changes an image to grayscale and reduces the contrast
    public static BufferedImage makeGrayscale(BufferedImage inImage) {
        int width = inImage.getWidth();
        int height = inImage.getHeight();
        BufferedImage outImg = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = inImage.getRGB(x, y);
                int a = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                // CIE luminance for the RGB
                int med = a > 0 ? 128 : 0;
                int v = (int) (0.2126 * (double) r + 0.7152 * (double) g + 0.0722 * (double) b);
                int avg = (med + v) / 2;

                int outPix = (a << 24) | (avg << 16) | (avg << 8) | (avg);
                outImg.setRGB(x, y, outPix);
            }
        }
        return outImg;
    }
}
