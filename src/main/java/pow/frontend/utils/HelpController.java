package pow.frontend.utils;

import pow.util.DebugLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HelpController {
    private static final HelpController instance;

    static {
        try {
            instance = new HelpController();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    public static List<String> getHelpText() {
        return instance.helpText;
    }

    private List<String> helpText;

    private HelpController() throws IOException {
        InputStream fileStream = this.getClass().getResourceAsStream("/help.txt");
        this.helpText = readLines(fileStream);
    }

    private List<String> readLines(InputStream stream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
