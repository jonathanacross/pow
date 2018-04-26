package pow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// Class for reading TSV files.
public class TsvReader {

    private List<String[]> data;

    public List<String[]> getData() {
        return this.data;
    }

    public TsvReader(File file) throws IOException {
        readFile(file);
    }

    public TsvReader(InputStream stream) throws IOException {
        readFile(stream);
    }

    private void readFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        readFile(is);
    }

    private void readFile(InputStream stream) throws IOException {
        data = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                if (line.trim().isEmpty()) continue;
                data.add(line.split("\t", -1));
            }
        }
    }
}
