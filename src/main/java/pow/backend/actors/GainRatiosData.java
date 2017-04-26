package pow.backend.actors;

import pow.util.DebugLogger;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GainRatiosData {
    private static final GainRatiosData instance;
    private Map<String, GainRatios> gainRatiosMap;

    static {
        try {
            instance = new GainRatiosData();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    public static GainRatios getGainRatios(String id) {
        if (!instance.gainRatiosMap.containsKey(id)) {
            //DebugLogger.info("warning: unknown gain ratio id '" + id + "'");
            return new GainRatios(id, 1.0, 1.0, 1.0, 1.0);
        }
        return instance.gainRatiosMap.get(id);
    }

    private GainRatiosData() throws IOException {
        // Get file from resources folder
        InputStream tsvStream = this.getClass().getResourceAsStream("/data/gainratios.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        gainRatiosMap = new HashMap<>();
        for (String[] line : reader.getData()) {
            GainRatios gr = parseGainRatios(line);
            gainRatiosMap.put(gr.id, gr);
        }
    }

    private static GainRatios parseGainRatios(String[] line) {
        String id;
        double strRatio;
        double dexRatio;
        double intRatio;
        double conRatio;

        if (line.length != 5) {
            throw new IllegalArgumentException("Expected 5 fields, but had " + line.length
                    + ". Fields = \n" + String.join(",", line));
        }

        try {
            id = line[0];
            strRatio = Double.parseDouble(line[1]);
            dexRatio = Double.parseDouble(line[2]);
            intRatio = Double.parseDouble(line[3]);
            conRatio = Double.parseDouble(line[4]);

            return new GainRatios(id, strRatio, dexRatio, intRatio, conRatio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage() + "\nFields = \n" + String.join(",", line), e);
        }
    }
}
