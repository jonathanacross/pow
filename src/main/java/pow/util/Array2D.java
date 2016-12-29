package pow.util;

// utilities for 2D arrays.. for now.  It's hard to make a simple
// 2D array class that will handle primitive types.
public class Array2D {
    // assumes entries in array are referenced as x,y

    public static <T> int width(T[][] data) { return data.length; }
    public static int width(int[][] data) { return data.length; }
    public static int width(char[][] data) { return data.length; }
    public static int width(boolean[][] data) { return data.length; }
    public static int width(float[][] data) { return data.length; }
    public static int width(double[][] data) { return data.length; }

    public static <T> int height(T[][] data) { return data[0].length; }
    public static int height(int[][] data) { return data[0].length; }
    public static int height(char[][] data) { return data[0].length; }
    public static int height(boolean[][] data) { return data[0].length; }
    public static int height(float[][] data) { return data[0].length; }
    public static int height(double[][] data) { return data[0].length; }
}
