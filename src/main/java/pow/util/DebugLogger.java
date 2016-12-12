package pow.util;

// A very minimal logger, that allows me to turn off debug logging easily.
// If something fancier is required, can turn to log4j or something.
public class DebugLogger {

    private static final boolean WRITE_OUTPUT = true;

    public static void info(String message) {
        if (WRITE_OUTPUT) {
            System.out.println(message);
        }
    }

    public static void error(String message) {
        if (WRITE_OUTPUT) {
            System.err.println(message);
        }
    }

    public static void fatal(Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }
}
