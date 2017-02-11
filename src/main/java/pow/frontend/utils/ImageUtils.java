package pow.frontend.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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

                // CIE luminance for RGB
                int med = a > 0 ? 128 : 0;
                int v = (int) (0.2126 * (double) r + 0.7152 * (double) g + 0.0722 * (double) b);
                int avg = (med + v) / 2;

                int outPix = (a << 24) | (avg << 16) | (avg << 8) | (avg);
                outImg.setRGB(x, y, outPix);
            }
        }
        return outImg;
    }

    public static Color getAverageColorOfSubImage(BufferedImage image, int left, int top, int width, int height) {
        return getAverageColorOfSubImageXYZAverage(image, left, top, width, height);
    }

    // adapted from http://www.easyrgb.com/index.php?X=MATH&H=02#text2
    // RGBA values are integers from 0 to 255
    //X from 0 to  95.047
    //Y from 0 to 100.000
    //Z from 0 to 108.883
    private static void RGBToXYZ(int[] rgb, double[] xyz) {
        double var_R = ( rgb[0] / 255.0 );        //R from 0 to 255
        double var_G = ( rgb[1] / 255.0 );        //G from 0 to 255
        double var_B = ( rgb[2] / 255.0 );        //B from 0 to 255

        if ( var_R > 0.04045 ) var_R = Math.pow((( var_R + 0.055 ) / 1.055 ), 2.4);
        else                   var_R = var_R / 12.92;
        if ( var_G > 0.04045 ) var_G = Math.pow((( var_G + 0.055 ) / 1.055 ), 2.4);
        else                   var_G = var_G / 12.92;
        if ( var_B > 0.04045 ) var_B = Math.pow((( var_B + 0.055 ) / 1.055 ), 2.4);
        else                   var_B = var_B / 12.92;

        var_R = var_R * 100;
        var_G = var_G * 100;
        var_B = var_B * 100;

        //Observer = 2 degrees, Illuminant = D65
        xyz[0] = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
        xyz[1] = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
        xyz[2] = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
    }

    // adapted from http://www.easyrgb.com/index.php?X=MATH&H=01#text1
    private static void XYZToRGB(double[] xyz, int[] rgb) {
        double var_X = xyz[0] / 100;        //X from 0 to  95.047
        double var_Y = xyz[1] / 100;        //Y from 0 to 100.000
        double var_Z = xyz[2] / 100;        //Z from 0 to 108.883

        double var_R = var_X *  3.2406 + var_Y * -1.5372 + var_Z * -0.4986;
        double var_G = var_X * -0.9689 + var_Y *  1.8758 + var_Z *  0.0415;
        double var_B = var_X *  0.0557 + var_Y * -0.2040 + var_Z *  1.0570;

        if ( var_R > 0.0031308 ) var_R = 1.055 * ( Math.pow(var_R, ( 1 / 2.4 )) ) - 0.055;
        else                     var_R = 12.92 * var_R;
        if ( var_G > 0.0031308 ) var_G = 1.055 * ( Math.pow(var_G, ( 1 / 2.4 )) ) - 0.055;
        else                     var_G = 12.92 * var_G;
        if ( var_B > 0.0031308 ) var_B = 1.055 * ( Math.pow(var_B, ( 1 / 2.4 )) ) - 0.055;
        else                     var_B = 12.92 * var_B;

        rgb[0] = (int) Math.round(var_R * 255);
        rgb[1] = (int) Math.round(var_G * 255);
        rgb[2] = (int) Math.round(var_B * 255);
    }

    // Returns the average color (of nontransparent pixels) of a
    // rectangular subset of an image.
    //
    // Averaging is done in CIE XYZ space, since this more accurately
    // matches human perception.  Note that this works MUCH better than
    // directly averaging across RGB values; e.g., walls that are 50% gray
    // look darker than walls that use a checkerboard black and white
    // pattern, and this averaging will account for this.
    private static Color getAverageColorOfSubImageXYZAverage(BufferedImage image, int left, int top, int width, int height) {
        double[] totals = new double[3];
        int[] rgb = new int[3];
        double[] xyz = new double[3];
        double numPixels = 0;
        for (int i = 0; i < width; i++) {
            int x = left + i;
            for (int j = 0; j < height; j++) {
                int y = top + j;
                int pixel = image.getRGB(x, y);
                int a = (pixel >> 24) & 0xFF;
                rgb[0] = (pixel >> 16) & 0xFF;
                rgb[1] = (pixel >> 8) & 0xFF;
                rgb[2] = pixel & 0xFF;
                RGBToXYZ(rgb, xyz);
                double alphaPercent = (double) a / 0xFF;  // 1 = opaque

                totals[0] += alphaPercent * xyz[0];
                totals[1] += alphaPercent * xyz[1];
                totals[2] += alphaPercent * xyz[2];
                numPixels += alphaPercent;
            }
        }

        if (numPixels == 0) {
            // a completely transparent or zero-sized image
            return Color.BLACK;
        }

        float xAvg = (float) totals[0] / (float) numPixels;
        float yAvg = (float) totals[1] / (float) numPixels;
        float zAvg = (float) totals[2] / (float) numPixels;
        double[] xyzavg = {xAvg, yAvg, zAvg};
        int[] rgbAvg = new int[3];
        XYZToRGB(xyzavg, rgbAvg);
        return new Color(rgbAvg[0], rgbAvg[1], rgbAvg[2]);
    }

    public static List<String> wrapText(String text, FontMetrics textMetrics, int width) {
        String[] words = text.split(" ");
        int nIndex = 0;
        List<String> lines = new ArrayList<>();
        while (nIndex < words.length) {
            StringBuilder line = new StringBuilder();
            line.append(words[nIndex++]);
            while ((nIndex < words.length) && (textMetrics.stringWidth(line + " " + words[nIndex]) < width)) {
                line.append(" " + words[nIndex]);
                nIndex++;
            }
            lines.add(line.toString());
        }
        return lines;
    }
}
